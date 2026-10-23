package YOUR.PACKAGE.presentation.main.ui

import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import YOUR.PACKAGE.core.ui.extensions.onBackPressedDispatcher
import YOUR.PACKAGE.core.ui.extensions.updateStatusBar
import YOUR.PACKAGE.presentation.R
import YOUR.PACKAGE.presentation.base.activity.BaseActivity
import YOUR.PACKAGE.presentation.databinding.ActivityMainBinding

/**
 * Template — copy to `:presentation` …/presentation/main/ui/
 * Replace YOUR.PACKAGE with applicationId root.
 *
 * Second Activity (exported=false, singleTask). Hosts nav_graph_main: Home tabs + in-app
 * LanguageFragment / PremiumFragment / SettingsFragment / ExitFragment / details.
 * Remove the bottom-nav pieces when the product has no tabs.
 */
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private val navController by lazy {
        (supportFragmentManager.findFragmentById(binding.fcvContainerMain.id) as NavHostFragment).navController
    }

    private val tabDestinations = setOf(R.id.homeFragment /*, R.id.prayerFragment, R.id.calendarFragment, R.id.settingsFragment */)

    private val destinationListener = NavController.OnDestinationChangedListener { _, destination, _ ->
        val isTab = destination.id in tabDestinations
        binding.bnvMain.isVisible = isTab
        // banner slot: show only on tabs once loaded (ads skills wire loadBannerAd / isBannerLoaded)
    }

    override fun onCreated() {
        updateStatusBar(darkIcons = true)
        setupBottomNavigation()
        navController.addOnDestinationChangedListener(destinationListener)
        onBackPressedDispatcher { handleBackPress() }
        // startInAppUpdate() / unblockAppOpen() / loadBannerAd(...) when those features are wired
    }

    private fun setupBottomNavigation() {
        binding.bnvMain.setupWithNavController(navController)
        binding.bnvMain.setOnItemSelectedListener { item ->
            navigateBottomTab(item.itemId)
            true
        }
    }

    private fun navigateBottomTab(itemId: Int) {
        val options = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setRestoreState(true)
            .setPopUpTo(navController.graph.startDestinationId, inclusive = false, saveState = true)
            .build()
        navController.navigate(itemId, null, options)
    }

    private fun handleBackPress() {
        val current = navController.currentDestination?.id
        when {
            current !in tabDestinations -> navController.popBackStack()
            current != navController.graph.startDestinationId -> binding.bnvMain.selectedItemId = navController.graph.startDestinationId
            else -> finishAndRemoveTask() // or navigate(R.id.action_main_to_exitFragment) when an Exit screen exists
        }
    }

    override fun onDestroy() {
        navController.removeOnDestinationChangedListener(destinationListener)
        super.onDestroy()
    }
}
