package mingosgit.josecr.torneoya.ui.screens.cuentaLocal

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun CuentaLocalScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { navController.navigate("partidos_locales") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Text("Partidos Locales")
        }
        Button(
            onClick = { navController.navigate("mis_jugadores") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Text("Mis jugadores")
        }
        Button(
            onClick = { navController.navigate("equipos_predefinidos") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Text("Equipos Predefinidos")
        }
        Button(
            onClick = { navController.navigate("administrar_partidos") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Administrar Partidos")
        }
    }
}
