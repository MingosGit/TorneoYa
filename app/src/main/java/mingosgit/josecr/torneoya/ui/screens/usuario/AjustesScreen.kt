package mingosgit.josecr.torneoya.ui.screens.ajustes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.viewmodel.usuario.GlobalUserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    navController: NavController,
    globalUserViewModel: GlobalUserViewModel
) {
    val opciones = listOf(
        "Mi cuenta",
        "Mi cuenta local",
        "Idioma",
        "Notificaciones",
        "Tema de la app",
        "Datos y privacidad",
        "Ayuda",
        "Créditos",
        "Sobre la aplicación"
    )

    val sesionOnlineActiva by globalUserViewModel.sesionOnlineActiva.collectAsState()
    var mostrarAlerta by remember { mutableStateOf(false) }

    if (mostrarAlerta) {
        AlertDialog(
            onDismissRequest = { mostrarAlerta = false },
            title = { Text("Inicia sesión") },
            text = { Text("Debes iniciar sesión o registrarte para acceder a tu cuenta online.") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarAlerta = false
                    navController.navigate("login")
                }) {
                    Text("Iniciar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarAlerta = false
                    navController.navigate("register")
                }) {
                    Text("Registrarme")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(opciones) { opcion ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            when (opcion) {
                                "Mi cuenta" -> {
                                    if (sesionOnlineActiva) {
                                        navController.navigate("mi_cuenta")
                                    } else {
                                        mostrarAlerta = true
                                    }
                                }
                                // otros casos futuros
                            }
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(text = opcion, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}
