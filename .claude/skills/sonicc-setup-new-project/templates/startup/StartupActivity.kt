package YOUR.PACKAGE.presentation.startup.ui

import android.content.Intent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import YOUR.PACKAGE.core.ui.extensions.onBackPressedDispatcher
import YOUR.PACKAGE.core.ui.extensions.updateStatusBar
import YOUR.PACKAGE.presentation.R
import YOUR.PACKAGE.presentation.base.activity.BaseActivity
import YOUR.PACKAGE.presentation.databinding.ActivityStartupBinding
import YOUR.PACKAGE.presentation.main.ui.MainActivity

/**
 * Template — copy to `:presentation` …/presentation/startup/ui/
 * Replace YOUR.PACKAGE with applicationId root.
 *
 * Launcher Activity (Theme.App.Starting). Hosts nav_graph_startup:
 * StartupFragment → StartupLanguageFragment → [StartupPremiumFragment] → StartupIntroFragment → MainActivity
 *                 → [StartupWelcomeBackFragment] → [StartupPremiumFragment] → MainActivity (returning user)
 */
class StartupActivity : BaseActivity<ActivityStartupBinding>(ActivityStartupBinding::inflate) {

    private val navController by lazy {
        (supportFragmentManager.findFragmentById(binding.fcvContainerStartup.id) as NavHostFragment).navController
    }

    private val destinationListener = NavController.OnDestinationChangedListener { _, destination, _ ->
        when (destination.id) {
            R.id.startupPremiumFragment -> binding.fcvContainerStartup.setBackgroundResource(R.color.premiumBackground)
            else -> binding.fcvContainerStartup.setBackgroundResource(android.R.color.transparent) // theme windowBackground
        }
    }

    // override fun getInsetsMode() = InsetsMode.EDGE_TO_EDGE  // only when the design is full-bleed

    override fun initSplashScreen() {
        installSplashScreen()
    }

    override fun onCreated() {
        updateStatusBar(darkIcons = true)
        navController.addOnDestinationChangedListener(destinationListener)
        onBackPressedDispatcher { } // startup funnel: back is blocked on every destination
    }

    fun moveToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        navController.removeOnDestinationChangedListener(destinationListener)
        super.onDestroy()
    }
}
