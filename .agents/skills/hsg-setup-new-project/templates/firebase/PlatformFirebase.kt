package YOUR.PACKAGE.core.platform.firebase

import android.os.Bundle
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.installations.FirebaseInstallations
import YOUR.PACKAGE.core.common.Constants.TAG_FIREBASE
import YOUR.PACKAGE.core.common.EventsProvider

/**
 * Template — copy to `:core-platform` …/firebase/
 * Replace `YOUR.PACKAGE` with the applicationId root.
 *
 * Ads revenue (`logRevenueEvent`) is optional — add only when the app has ads.
 * Pass `Context` as an argument; never store it on this object.
 * Do not copy another app's prefs names (`rossPref`, `TaichiTroasCache`).
 * Do not log the Installation token value (secrets / 14-security-secrets).
 */
object PlatformFirebase {

    private var currentScreen: String? = null
    private var previousScreen: String? = null
    private var entrySource: String = EventsProvider.ENTRY_ORGANIC

    fun setEntrySource(source: String) {
        entrySource = source
    }

    fun Throwable.recordException(log: String) {
        Log.e(TAG_FIREBASE, "PlatformFirebase: recordException: Failed: $log")
        FirebaseCrashlytics.getInstance().log(log)
        FirebaseCrashlytics.getInstance().recordException(this)
    }

    fun postScreenView(screenName: String, screenClass: String) {
        val screenBefore = when {
            currentScreen != null && currentScreen != screenName -> currentScreen
            else -> previousScreen
        }
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
            screenBefore?.let { putString(EventsProvider.PREVIOUS_SCREEN, it) }
            putString(EventsProvider.ENTRY_SOURCE, entrySource)
        }
        logEvent("postScreenView", EventsProvider.SCREEN_VIEW, bundle)
        if (currentScreen != null && currentScreen != screenName) {
            previousScreen = currentScreen
        }
        currentScreen = screenName
    }

    fun postUiClick(screenName: String, elementName: String, elementType: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(EventsProvider.ELEMENT_NAME, elementName)
            putString(EventsProvider.ELEMENT_TYPE, elementType)
        }
        logEvent("postUiClick", EventsProvider.UI_CLICK, bundle)
    }

    private fun logEvent(functionName: String, eventName: String, bundle: Bundle) {
        runCatching {
            Firebase.analytics.logEvent(eventName, bundle)
        }.onSuccess {
            Log.d(TAG_FIREBASE, "PlatformFirebase: $functionName: Success: event=$eventName")
        }.onFailure { error ->
            Log.e(TAG_FIREBASE, "PlatformFirebase: $functionName: Failed: event=$eventName ${error.message}")
        }
    }

    fun getDeviceToken() {
        FirebaseInstallations.getInstance().getToken(false)
            .addOnCompleteListener { task ->
                when {
                    task.isSuccessful && task.result != null ->
                        Log.d(TAG_FIREBASE, "PlatformFirebase: getDeviceToken: Success")
                    else ->
                        Log.e(TAG_FIREBASE, "PlatformFirebase: getDeviceToken: Failed")
                }
            }
    }
}
