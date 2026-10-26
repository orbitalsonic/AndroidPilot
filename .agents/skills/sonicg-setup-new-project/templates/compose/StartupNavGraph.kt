package YOUR.PACKAGE.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import YOUR.PACKAGE.feature.startup.STARTUP_ROUTE
import YOUR.PACKAGE.feature.startup.StartupScreen

/**
 * Hosted by StartupActivity. Funnel only: Startup → StartupLanguage → [StartupPremium] → StartupIntro,
 * or Startup → [StartupWelcomeBack] → [StartupPremium] for returning users. `onFinished` moves to MainActivity.
 */
@Composable
fun StartupNavGraph(
    modifier: Modifier = Modifier,
    onFinished: () -> Unit,
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = STARTUP_ROUTE,
        modifier = modifier.fillMaxSize(),
        enterTransition = { slideInHorizontally { it } },
        exitTransition = { slideOutHorizontally { -it } },
        popEnterTransition = { slideInHorizontally { -it } },
        popExitTransition = { slideOutHorizontally { it } },
    ) {
        composable(route = STARTUP_ROUTE) {
            StartupScreen(
                navigateToNext = {
                    // sonicg-mvi wires STARTUP_LANGUAGE_ROUTE / STARTUP_WELCOME_BACK_ROUTE with popUpTo(STARTUP_ROUTE) { inclusive = true }
                },
                navigateToMain = onFinished,
            )
        }
    }
}
