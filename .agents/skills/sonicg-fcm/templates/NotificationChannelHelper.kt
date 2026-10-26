package YOUR.PACKAGE.core.platform.firebase.fcm.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import YOUR.PACKAGE.core.platform.R

/**
 * Template — copy to `:core-platform` …/core/platform/firebase/fcm/notification/.
 * Replace YOUR.PACKAGE with applicationId root.
 *
 * Creates the default FCM channel once (QiblaCompass `NotificationChannelHelper`).
 * Channel id / name come from res/values/firebase.xml (same id as the manifest meta-data).
 */
object NotificationChannelHelper {

    fun create(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channelId = context.getString(R.string.default_notification_channel_id)
        val channelName = context.getString(R.string.default_notification_channel_name)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(channelId) == null) {
            manager.createNotificationChannel(
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            )
        }
    }
}
