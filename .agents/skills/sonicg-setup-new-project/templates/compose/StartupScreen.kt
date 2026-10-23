package YOUR.PACKAGE.feature.startup

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import YOUR.PACKAGE.core.ui.theme.AppTheme

const val STARTUP_ROUTE = "startup"

/**
 * Start destination of StartupNavGraph (StartupActivity). Init + routing only — see 29-screen-startup.
 * Sibling routes added by sonicg-create-mvi: STARTUP_LANGUAGE_ROUTE, STARTUP_PREMIUM_ROUTE ("startup_premium/{isFirstTime}"),
 * STARTUP_INTRO_ROUTE, STARTUP_WELCOME_BACK_ROUTE.
 */
@Composable
fun StartupScreen(
    modifier: Modifier = Modifier,
    navigateToNext: () -> Unit,
    navigateToMain: () -> Unit,
) {
    BackHandler { /* startup funnel: back blocked */ }
    StartupScreenContent(modifier = modifier)
}

@Composable
private fun StartupScreenContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Preview(showBackground = true)
@Composable
private fun StartupScreenPreview() {
    AppTheme {
        StartupScreenContent()
    }
}
