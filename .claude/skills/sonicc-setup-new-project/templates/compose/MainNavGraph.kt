package YOUR.PACKAGE.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import YOUR.PACKAGE.feature.home.HOME_ROUTE
import YOUR.PACKAGE.feature.home.HomeScreen

/**
 * Hosted by MainActivity. Home / tabs + in-app LANGUAGE_ROUTE / PREMIUM_ROUTE / settings / details.
 * Never contains startup routes.
 */
@Composable
fun MainNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = HOME_ROUTE,
        modifier = modifier.fillMaxSize(),
        enterTransition = { slideInHorizontally { it } },
        exitTransition = { slideOutHorizontally { -it } },
        popEnterTransition = { slideInHorizontally { -it } },
        popExitTransition = { slideOutHorizontally { it } },
    ) {
        composable(route = HOME_ROUTE) {
            HomeScreen(
                // onNavigateToPremium = { navController.navigate(PREMIUM_ROUTE) } — added by sonicc-create-mvi
            )
        }
    }
}
