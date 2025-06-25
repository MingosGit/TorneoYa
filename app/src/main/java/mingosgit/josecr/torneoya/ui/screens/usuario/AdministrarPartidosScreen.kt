package mingosgit.josecr.torneoya.ui.screens.usuario

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.viewmodel.usuario.AdministrarPartidosViewModel
import mingosgit.josecr.torneoya.data.entities.PartidoEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdministrarPartidosScreen(
    partido: PartidoEntity,
    viewModel: AdministrarPartidosViewModel,
    navController: NavController
) {
    var golesA by remember { mutableStateOf(partido.golesEquipoA) }
    var golesB by remember { mutableStateOf(partido.golesEquipoB) }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Administrar Goles del Partido") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.Remove, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ID: ${partido.id} | Fecha: ${partido.fecha}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Equipo A", style = MaterialTheme.typography.bodyLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (golesA > 0) golesA--
                            },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.Red)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Quitar gol equipo A")
                        }
                        Text(
                            text = golesA.toString(),
                            modifier = Modifier.width(24.dp)
                        )
                        IconButton(
                            onClick = { golesA++ },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.Green)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Agregar gol equipo A")
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Equipo B", style = MaterialTheme.typography.bodyLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (golesB > 0) golesB--
                            },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.Red)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Quitar gol equipo B")
                        }
                        Text(
                            text = golesB.toString(),
                            modifier = Modifier.width(24.dp)
                        )
                        IconButton(
                            onClick = { golesB++ },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.Green)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Agregar gol equipo B")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    showDialog = true
                }
            ) {
                Text("Guardar Cambios")
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Confirmar cambios") },
                    text = { Text("¿Guardar los nuevos goles?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.actualizarGoles(partido, golesA, golesB)
                                showDialog = false
                                navController.popBackStack()
                            }
                        ) { Text("Guardar") }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}
