package YOUR.PACKAGE.core.platform.firebase.fcm.model

/**
 * Template — copy to `:core-platform` …/core/platform/firebase/fcm/model/.
 * Replace YOUR.PACKAGE with applicationId root.
 *
 * Value of the `type` key in the FCM **data** payload (QiblaCompass). Unknown / missing → GENERAL.
 */
enum class NotificationType {
    GENERAL,        // Open the app (launch intent)
    EXTERNAL_LINK,  // Open `url` outside the app
    REENGAGEMENT,   // Bring inactive users back — gated by `days` since first open
}
