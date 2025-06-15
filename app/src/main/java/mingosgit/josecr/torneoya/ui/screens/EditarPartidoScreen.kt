package mingosgit.josecr.torneoya.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mingosgit.josecr.torneoya.viewmodel.AppViewModelFactory
import mingosgit.josecr.torneoya.viewmodel.PartidoViewModel
import androidx.compose.ui.Alignment
import androidx.navigation.NavController

@Composable
fun EditarPartidoScreen(
    partidoId: Long,
    onPartidoEditado: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current.applicationContext
    val factory = remember { AppViewModelFactory(context) }
    val viewModel: PartidoViewModel = viewModel(
        modelClass = PartidoViewModel::class.java,
        factory = factory
    )

    val ui by viewModel.ui.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(partidoId) {
        viewModel.cargarPartidoCompleto(partidoId)
    }

    if (ui == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(Modifier
        .fillMaxSize()
        .padding(24.dp)) {
        Text("Editar Partido", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        Text("Equipo local: ${ui!!.nombreEquipoLocal}", style = MaterialTheme.typography.titleMedium)
        Text("Integrantes:", style = MaterialTheme.typography.bodyMedium)
        ui!!.integrantesLocal.forEach { nombre ->
            Text("- $nombre")
        }

        Spacer(Modifier.height(16.dp))

        Text("Equipo visitante: ${ui!!.nombreEquipoVisitante}", style = MaterialTheme.typography.titleMedium)
        Text("Integrantes:", style = MaterialTheme.typography.bodyMedium)
        ui!!.integrantesVisitante.forEach { nombre ->
            Text("- $nombre")
        }

        Spacer(Modifier.height(16.dp))
        Text("Fecha (timestamp): ${ui!!.partido.fecha}", style = MaterialTheme.typography.bodySmall)

        Spacer(Modifier.height(32.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                navController.navigate("editarIntegrantes/${ui!!.partido.id}")
            }
        ) {
            Text("Editar nombres de los integrantes")
        }

        Spacer(Modifier.height(16.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            onClick = {
                showDeleteDialog = true
            }
        ) {
            Text("Eliminar partido")
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar partido") },
            text = { Text("¿Estás seguro de que quieres eliminar este partido? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.eliminarPartidoActual {
                        onPartidoEditado()
                    }
                }) { Text("Sí, eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }
}
