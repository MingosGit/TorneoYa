package mingosgit.josecr.torneoya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import mingosgit.josecr.torneoya.ui.theme.TorneoYaTheme
import mingosgit.josecr.torneoya.ui.navigation.AppNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TorneoYaTheme {
                AppNavHost()
            }
        }
    }
}
