package mingosgit.josecr.torneoya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import mingosgit.josecr.torneoya.ui.theme.TorneoYaTheme
import mingosgit.josecr.torneoya.ui.navigation.BottomNavigationBar
import mingosgit.josecr.torneoya.ui.navigation.NavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TorneoYaTheme {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = { BottomNavigationBar(navController) },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Column(modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)) {
                        NavGraph(navController = navController)
                    }
                }
            }
        }
    }
}
