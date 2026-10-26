package YOUR.PACKAGE.core.platform.firebase.fcm.notification

import android.util.Log
import YOUR.PACKAGE.core.common.Constants.TAG_FCM
import YOUR.PACKAGE.core.platform.firebase.fcm.model.NotificationPayload
import YOUR.PACKAGE.core.platform.firebase.fcm.model.NotificationType

/**
 * Template — copy to `:core-platform` …/core/platform/firebase/fcm/notification/.
 * Replace YOUR.PACKAGE with applicationId root.
 *
 * Maps the FCM **data** map to a typed payload (QiblaCompass `NotificationPayloadMapper`).
 * Returns null when `title` / `body` or a type-specific key (`url`, `days`) is missing.
 * Unknown / missing `type` falls back to GENERAL so old campaigns keep working.
 */
object NotificationPayloadMapper {

    fun from(data: Map<String, String>): NotificationPayload? {
        val type = data["type"]?.let { raw ->
            runCatching { NotificationType.valueOf(raw.trim().uppercase()) }.getOrNull()
        } ?: NotificationType.GENERAL

        val title = data["title"]?.takeIf { it.isNotBlank() } ?: return reject("title")
        val body = data["body"]?.takeIf { it.isNotBlank() } ?: return reject("body")
        val icon = data["icon"]?.takeIf { it.isNotBlank() }
        val feature = data["feature"]?.takeIf { it.isNotBlank() }

        return when (type) {
            NotificationType.GENERAL -> NotificationPayload.General(title, body, icon, feature)

            NotificationType.EXTERNAL_LINK -> NotificationPayload.ExternalLink(
                title = title,
                body = body,
                icon = icon,
                feature = feature,
                url = data["url"]?.takeIf { it.isNotBlank() } ?: return reject("url"),
            )

            NotificationType.REENGAGEMENT -> NotificationPayload.Reengagement(
                title = title,
                body = body,
                icon = icon,
                feature = feature,
                days = data["days"]?.takeIf { it.isNotBlank() } ?: return reject("days"),
            )
        }
    }

    private fun reject(missingKey: String): NotificationPayload? {
        Log.w(TAG_FCM, "NotificationPayloadMapper: from: Rejected: missing=$missingKey")
        return null
    }
}
