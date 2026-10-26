package YOUR.PACKAGE.core.platform.firebase.fcm.model

/**
 * Template — copy to `:core-platform` …/core/platform/firebase/fcm/model/.
 * Replace YOUR.PACKAGE with applicationId root.
 *
 * Typed FCM data payload (QiblaCompass `NotificationPayload`). Built only by
 * `NotificationPayloadMapper`; consumed in-process by `NotificationSender` — no Parcelable needed.
 */
sealed class NotificationPayload(
    open val title: String,
    open val body: String,
    open val icon: String? = null,     // small image URL (60dp)
    open val feature: String? = null,  // large image URL under the text
) {

    data class General(
        override val title: String,
        override val body: String,
        override val icon: String? = null,
        override val feature: String? = null,
    ) : NotificationPayload(title, body, icon, feature)

    data class ExternalLink(
        override val title: String,
        override val body: String,
        override val icon: String? = null,
        override val feature: String? = null,
        val url: String,
    ) : NotificationPayload(title, body, icon, feature)

    data class Reengagement(
        override val title: String,
        override val body: String,
        override val icon: String? = null,
        override val feature: String? = null,
        val days: String,  // "-N" exact day, "N" on/after day N, "0" never
    ) : NotificationPayload(title, body, icon, feature)
}
