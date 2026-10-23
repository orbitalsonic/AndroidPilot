package YOUR.PACKAGE.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import YOUR.PACKAGE.core.ui.theme.AppTheme
import YOUR.PACKAGE.navigation.MainNavGraph

/** exported=false, singleTask. Hosts MainNavGraph (home / tabs / in-app Language, Premium, Settings). */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                MainNavGraph()
            }
        }
    }
}
