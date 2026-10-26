package YOUR.PACKAGE.core.platform.firebase.fcm.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import YOUR.PACKAGE.core.common.Constants
import YOUR.PACKAGE.core.common.Constants.TAG_FCM
import YOUR.PACKAGE.core.platform.R
import YOUR.PACKAGE.core.platform.firebase.fcm.model.NotificationPayload
import kotlin.math.abs

/**
 * Template — copy to `:core-platform` …/core/platform/firebase/fcm/notification/.
 * Replace YOUR.PACKAGE with applicationId root.
 *
 * Only class that talks to NotificationManager (QiblaCompass `NotificationSender`).
 * Custom RemoteViews (res/layout/firebase_notification_view.xml): title, body, optional icon +
 * feature image loaded with Glide after the first notify, day/night text colors.
 *
 * Re-engagement gate: `days < 0` exact day, `days > 0` on/after, `0` never. Days spent are counted
 * from `Constants.PREF_REENGAGEMENT_DATE` in the app prefs file (`Constants.PREFS_NAME`) — Startup
 * writes that value once (skill Step 4). The service runs without Koin scope, so prefs are read
 * directly by file + key shared with `SharedPrefManager`.
 */
object NotificationSender {

    private const val MILLIS_PER_DAY = 1000L * 60 * 60 * 24

    fun send(context: Context, payload: NotificationPayload) {
        if (payload is NotificationPayload.Reengagement) {
            val days = payload.days.toIntOrNull()
            if (days == null || !shouldSendReengagement(context, days)) {
                Log.d(TAG_FCM, "NotificationSender: send: Skipped: reengagement days=${payload.days}")
                return
            }
        }
        NotificationChannelHelper.create(context)
        showNotification(context, payload)
    }

    private fun showNotification(context: Context, payload: NotificationPayload) {
        val intent = NotificationNavigation.create(context, payload)
        val pendingIntent = PendingIntent.getActivity(
            context,
            (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val channelId = context.getString(R.string.default_notification_channel_id)
        val remoteViews = RemoteViews(context.packageName, R.layout.firebase_notification_view).apply {
            setTextViewText(R.id.tvFcmTitle, payload.title)
            setTextViewText(R.id.tvFcmBody, payload.body)
            val textColor = if (isSystemInDarkMode(context)) Color.WHITE else Color.BLACK
            setTextColor(R.id.tvFcmTitle, textColor)
            setTextColor(R.id.tvFcmBody, textColor)
            setViewVisibility(R.id.ivFcmIcon, if (payload.icon.isNullOrEmpty()) View.GONE else View.VISIBLE)
            setViewVisibility(R.id.ivFcmFeature, if (payload.feature.isNullOrEmpty()) View.GONE else View.VISIBLE)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_fcm_notification_small)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setCustomContentView(remoteViews)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()

        notificationManager.notify(notificationId, builder.build())
        Log.d(TAG_FCM, "NotificationSender: showNotification: Success: type=${payload::class.simpleName}")

        // Images arrive after the first notify; each load re-posts the same id.
        payload.icon?.takeIf { it.isNotEmpty() }?.let { url ->
            loadInto(context, url, remoteViews, R.id.ivFcmIcon, builder, notificationManager, notificationId)
        }
        payload.feature?.takeIf { it.isNotEmpty() }?.let { url ->
            loadInto(context, url, remoteViews, R.id.ivFcmFeature, builder, notificationManager, notificationId)
        }
    }

    private fun loadInto(
        context: Context,
        url: String,
        remoteViews: RemoteViews,
        viewId: Int,
        builder: NotificationCompat.Builder,
        notificationManager: NotificationManager,
        notificationId: Int,
    ) {
        runCatching {
            Glide.with(context.applicationContext)
                .asBitmap()
                .load(url)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        remoteViews.setViewVisibility(viewId, View.VISIBLE)
                        remoteViews.setImageViewBitmap(viewId, resource)
                        notificationManager.notify(notificationId, builder.build())
                    }

                    override fun onLoadCleared(placeholder: Drawable?) = Unit

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        remoteViews.setViewVisibility(viewId, View.GONE)
                        notificationManager.notify(notificationId, builder.build())
                    }
                })
        }.onFailure { Log.e(TAG_FCM, "NotificationSender: loadInto: Failed: ${it.message}") }
    }

    private fun shouldSendReengagement(context: Context, days: Int): Boolean {
        if (days == 0) return false
        val daysSpent = getDaysSpent(context)
        val target = abs(days)
        return if (days < 0) daysSpent == target else daysSpent >= target
    }

    private fun getDaysSpent(context: Context): Int {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val start = prefs.getLong(Constants.PREF_REENGAGEMENT_DATE, 0L)
        if (start == 0L) return 0
        return ((System.currentTimeMillis() - start) / MILLIS_PER_DAY).toInt()
    }

    private fun isSystemInDarkMode(context: Context): Boolean =
        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
}
