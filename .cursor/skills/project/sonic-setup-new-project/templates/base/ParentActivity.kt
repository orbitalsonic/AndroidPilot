package YOUR.PACKAGE.core.ui.base.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewbinding.ViewBinding
import com.google.android.material.color.DynamicColors
import YOUR.PACKAGE.core.common.constants.Constants.TAG
import YOUR.PACKAGE.core.ui.base.enums.InsetsMode

/**
 * Template — copy to `:core-ui` …/base/activity/
 * Replace YOUR.PACKAGE with applicationId root.
 *
 * Edge-to-edge via [InsetsMode] (reference: QiblaCompass BaseActivity).
 * onCreate order is fixed: initSplashScreen() → super.onCreate → setupInsets(getInsetsMode()) → onCreated() → initObservers()
 */
abstract class ParentActivity<T : ViewBinding>(
    private val bindingFactory: (LayoutInflater) -> T,
) : AppCompatActivity() {

    protected val binding: T by lazy { bindingFactory(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        initSplashScreen()
        super.onCreate(savedInstanceState)

        setupInsets(getInsetsMode())
        onCreated()
        initObservers()
    }

    private fun setupInsets(mode: InsetsMode) {
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime())

            val bottomInset = if (isKeyboardVisible) imeInsets.bottom else systemBars.bottom

            val top = when (mode) {
                InsetsMode.TOP, InsetsMode.TOP_BOTTOM -> systemBars.top
                else -> 0
            }

            val bottom = when (mode) {
                InsetsMode.BOTTOM, InsetsMode.TOP_BOTTOM -> bottomInset
                else -> 0
            }

            view.setPadding(systemBars.left, top, systemBars.right, bottom)

            WindowInsetsCompat.CONSUMED
        }
    }

    /**
     * Override per Activity:
     * `override fun getInsetsMode() = InsetsMode.EDGE_TO_EDGE`
     */
    open fun getInsetsMode(): InsetsMode = InsetsMode.TOP_BOTTOM

    /**
     * Runs before `super.onCreate`. Only StartupActivity overrides this with `installSplashScreen()`.
     */
    open fun initSplashScreen() {}

    open fun initObservers() {}

    abstract fun onCreated()

    protected open fun enableMaterialDynamicTheme() {
        Log.d(TAG, "ParentActivity: enableMaterialDynamicTheme: Started")
        DynamicColors.applyToActivityIfAvailable(this)
    }

    protected open fun hideStatusBar(type: Int) {
        Log.d(TAG, "ParentActivity: hideStatusBar: type=$type")
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior = when (type) {
                0 -> WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
                else -> WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            when (type) {
                0 -> show(WindowInsetsCompat.Type.systemBars())
                1 -> hide(WindowInsetsCompat.Type.systemBars())
                2 -> hide(WindowInsetsCompat.Type.statusBars())
                3 -> hide(WindowInsetsCompat.Type.navigationBars())
                else -> hide(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
}
