package YOUR.PACKAGE.core.platform.firebase.fcm.notification

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import YOUR.PACKAGE.core.platform.firebase.fcm.model.NotificationPayload

/**
 * Template — copy to `:core-platform` …/core/platform/firebase/fcm/notification/.
 * Replace YOUR.PACKAGE with applicationId root.
 *
 * Tap intent per payload (QiblaCompass `NotificationNavigation`). Uses the **launch intent**
 * (StartupActivity is the launcher — 23-app-startup) so `:core-platform` never imports presentation.
 */
object NotificationNavigation {

    fun create(context: Context, payload: NotificationPayload): Intent = when (payload) {
        is NotificationPayload.General,
        is NotificationPayload.Reengagement -> launchIntent(context)

        is NotificationPayload.ExternalLink -> Intent(Intent.ACTION_VIEW, payload.url.toUri())
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    private fun launchIntent(context: Context): Intent =
        context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP) }
            ?: Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setPackage(context.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
}
