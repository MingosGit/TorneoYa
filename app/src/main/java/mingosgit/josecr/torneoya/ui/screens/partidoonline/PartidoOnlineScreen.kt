package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import mingosgit.josecr.torneoya.viewmodel.partidoonline.PartidoOnlineViewModel

@Composable
fun PartidoOnlineScreen(
    navController: NavController,
    partidoViewModel: PartidoOnlineViewModel
) {
    val currentUser = FirebaseAuth.getInstance().currentUser

    if (currentUser == null) {
        LoginOrRegisterPrompt(
            navController = navController
        )
    } else {
        PartidoOnlineScreenContent(
            navController = navController,
            partidoViewModel = partidoViewModel
        )
    }
}

@Composable
private fun LoginOrRegisterPrompt(
    navController: NavController
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Debes iniciar sesión o crear una cuenta para ver los partidos online.",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { navController.navigate("login") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Iniciar sesión")
                }
                OutlinedButton(
                    onClick = { navController.navigate("register") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Crear cuenta")
                }
            }
        }
    }
}
