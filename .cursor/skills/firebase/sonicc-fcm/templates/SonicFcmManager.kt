package YOUR.PACKAGE.core.platform.firebase.fcm

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import YOUR.PACKAGE.core.common.Constants.TAG_FCM
import YOUR.PACKAGE.core.platform.firebase.fcm.topic.FcmTopics
import YOUR.PACKAGE.core.platform.firebase.fcm.topic.TokenType

/**
 * Template — copy to `:core-platform` …/core/platform/firebase/fcm/ (single-module: common/fcm/).
 * Replace YOUR.PACKAGE with applicationId root.
 *
 * QiblaCompass `SonicFcmManager` shape. Pure `object`: callers pass language / premium values —
 * never read prefs or billing here through GlobalContext / a DI façade (07-dependency-injection).
 *
 * Hosts:
 *  - MainActivity.onCreated()           → setupFcm(applicationContext, languageCode, isPremium)
 *  - StartupLanguage Next / Language Apply → updateLanguage(old, new)
 *  - StartupPremium / Premium purchase  → updatePremium(isPremium)
 */
object SonicFcmManager {

    suspend fun setupFcm(context: Context, languageCode: String, isPremium: Boolean) {
        val messaging = FirebaseMessaging.getInstance()
        messaging.subscribe(FcmTopics.getTopic(TokenType.AllUsers(context)))
        messaging.subscribe(FcmTopics.getTopic(TokenType.Language(languageCode)))
        messaging.subscribe(FcmTopics.getTopic(TokenType.Country(context)))
        messaging.applyPremium(isPremium)
        Log.d(TAG_FCM, "SonicFcmManager: setupFcm: Success: language=$languageCode premium=$isPremium")
    }

    suspend fun updateLanguage(old: String, new: String) {
        if (old.equals(new, ignoreCase = true)) return
        val messaging = FirebaseMessaging.getInstance()
        messaging.subscribe(FcmTopics.getTopic(TokenType.Language(new)))
        messaging.unsubscribe(FcmTopics.getTopic(TokenType.Language(old)))
        Log.d(TAG_FCM, "SonicFcmManager: updateLanguage: Success: $old -> $new")
    }

    suspend fun updatePremium(isPremium: Boolean) {
        FirebaseMessaging.getInstance().applyPremium(isPremium)
        Log.d(TAG_FCM, "SonicFcmManager: updatePremium: Success: premium=$isPremium")
    }

    private suspend fun FirebaseMessaging.applyPremium(isPremium: Boolean) {
        val premium = FcmTopics.getTopic(TokenType.PremiumUsers)
        val nonPremium = FcmTopics.getTopic(TokenType.NonPremiumUsers)
        if (isPremium) {
            subscribe(premium)
            unsubscribe(nonPremium)
        } else {
            subscribe(nonPremium)
            unsubscribe(premium)
        }
    }

    private suspend fun FirebaseMessaging.subscribe(topic: String) {
        runCatching { subscribeToTopic(topic).await() }
            .onFailure { Log.e(TAG_FCM, "SonicFcmManager: subscribe: Failed: topic=$topic ${it.message}") }
    }

    private suspend fun FirebaseMessaging.unsubscribe(topic: String) {
        runCatching { unsubscribeFromTopic(topic).await() }
            .onFailure { Log.e(TAG_FCM, "SonicFcmManager: unsubscribe: Failed: topic=$topic ${it.message}") }
    }
}
