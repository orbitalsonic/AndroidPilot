package YOUR.PACKAGE.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import YOUR.PACKAGE.core.ui.theme.AppTheme
import YOUR.PACKAGE.navigation.StartupNavGraph

/** Launcher (Theme.App.Starting). Hosts the startup funnel; hands off to MainActivity. */
class StartupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                StartupNavGraph(
                    onFinished = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    },
                )
            }
        }
    }
}
