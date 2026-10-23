package YOUR.PACKAGE.core.ui.extensions

import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withCreated
import androidx.lifecycle.withResumed
import androidx.lifecycle.withStarted
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import YOUR.PACKAGE.core.common.constants.Constants.TAG

/**
 * Template — copy to `:core-ui` …/extensions/ActivityExtensions.kt
 * Replace YOUR.PACKAGE with applicationId root.
 *
 * State → collectWhenStarted; Effects → collectWhenCreated (typical MVI).
 */

fun AppCompatActivity.onBackPressedDispatcher(callback: () -> Unit) {
    onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            callback.invoke()
        }
    })
}

fun AppCompatActivity.repeatWhen(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    block: suspend () -> Unit,
) {
    lifecycleScope.launch {
        repeatOnLifecycle(lifecycleState) {
            block()
        }
    }
}

fun <T> AppCompatActivity.collectWhenCreated(flow: Flow<T>, collector: suspend (T) -> Unit) {
    repeatWhen(lifecycleState = Lifecycle.State.CREATED) {
        flow.collect(collector)
    }
}

fun <T> AppCompatActivity.collectWhenStarted(flow: Flow<T>, collector: suspend (T) -> Unit) {
    repeatWhen(lifecycleState = Lifecycle.State.STARTED) {
        flow.collect(collector)
    }
}

fun AppCompatActivity.launchWhenCreated(callback: () -> Unit) {
    lifecycleScope.launch { lifecycle.withCreated(callback) }
}

fun AppCompatActivity.launchWhenStarted(callback: () -> Unit) {
    lifecycleScope.launch { lifecycle.withStarted(callback) }
}

fun AppCompatActivity.launchWhenResumed(callback: () -> Unit) {
    lifecycleScope.launch { lifecycle.withResumed(callback) }
}

/**
 * Light / dark status-bar icons (no statusBarColor — leave window colors to the theme).
 * StartupActivity / MainActivity call this from onCreated().
 */
fun AppCompatActivity.updateStatusBar(darkIcons: Boolean) {
    runCatching {
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = darkIcons
    }.onFailure {
        Log.e(TAG, "ActivityExtensions: updateStatusBar: Failed: ${it.message}")
    }
}
