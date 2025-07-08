package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.viewmodel.partidoonline.VisualizarPartidoOnlineViewModel
import mingosgit.josecr.torneoya.viewmodel.partidoonline.VisualizarPartidoOnlineUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualizarPartidoOnlineScreen(
    partidoUid: String,
    navController: NavController,
    vm: VisualizarPartidoOnlineViewModel,
    usuarioUid: String
) {
    LaunchedEffect(partidoUid) { vm.cargarDatos(usuarioUid) }
    val uiState by vm.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val eliminado by vm.eliminado.collectAsState()

    LaunchedEffect(eliminado) {
        if (eliminado) {
            navController.navigate("online") {
                popUpTo("online") { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Visualizar Partido Online") },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        VisualizarPartidoOnlineContent(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState,
            vm = vm,
            usuarioUid = usuarioUid,
            partidoUid = partidoUid
        )
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        vm.eliminarPartido()
                    }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
                },
                title = { Text("Eliminar Partido") },
                text = { Text("¿Seguro que deseas eliminar este partido? Esta acción no se puede deshacer.") }
            )
        }
    }
}
