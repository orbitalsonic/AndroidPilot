package YOUR.PACKAGE.core.platform.firebase.fcm.notification

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import YOUR.PACKAGE.core.common.Constants.TAG_FCM

/**
 * Template — copy to `:core-platform` …/core/platform/firebase/fcm/notification/.
 * Replace YOUR.PACKAGE with applicationId root.
 *
 * QiblaCompass `SonicMessagingService`. Register in the hosting module manifest:
 *   <service android:name=".firebase.fcm.notification.SonicMessagingService" android:exported="false">
 *       <intent-filter><action android:name="com.google.firebase.MESSAGING_EVENT" /></intent-filter>
 *   </service>
 *
 * Handles **data** messages only. Never logs or persists the token (14-security-secrets).
 */
class SonicMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        if (!hasNotificationPermission()) {
            Log.w(TAG_FCM, "SonicMessagingService: onMessageReceived: Skipped: POST_NOTIFICATIONS not granted")
            return
        }
        val payload = NotificationPayloadMapper.from(message.data) ?: return
        NotificationSender.send(this, payload)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Topics re-attach automatically. Do not log or persist the token.
        Log.d(TAG_FCM, "SonicMessagingService: onNewToken: Success")
    }

    private fun hasNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
}
