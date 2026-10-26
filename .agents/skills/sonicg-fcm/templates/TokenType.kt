package YOUR.PACKAGE.core.platform.firebase.fcm.topic

import android.content.Context

/**
 * Template — copy to `:core-platform` …/core/platform/firebase/fcm/topic/.
 * Replace YOUR.PACKAGE with applicationId root.
 *
 * Topic kinds (QiblaCompass `TokenType`). Resolve to a topic string with `FcmTopics.getTopic(...)`.
 */
sealed class TokenType {
    /** Every install of this app — topic name is `applicationId`. */
    data class AllUsers(val context: Context) : TokenType()
    object PremiumUsers : TokenType()
    object NonPremiumUsers : TokenType()

    /** Catalog language code (`en`, `zh-CN`, `ceb`, …) — see rules/reference/languages.md. */
    data class Language(val languageCode: String) : TokenType()

    /** SIM country ISO via TelephonyManager (falls back to `Unknown`). */
    data class Country(val context: Context) : TokenType()
}
