package mingosgit.josecr.torneoya.ui.screens.partido

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import mingosgit.josecr.torneoya.ui.screens.partido.visualizarpartidoscreen.PartidoTopBar
import mingosgit.josecr.torneoya.ui.screens.partido.visualizarpartidoscreen.VisualizarPartidoContent
import mingosgit.josecr.torneoya.viewmodel.partido.VisualizarPartidoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualizarPartidoScreen(
    partidoId: Long,
    navController: NavController,
    vm: VisualizarPartidoViewModel
) {
    LaunchedEffect(partidoId) { vm.cargarDatos() }
    val uiState by vm.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val eliminado by vm.eliminado.collectAsState()
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry.value) {
        val recargar = navController.previousBackStackEntry?.arguments?.getBoolean("reload_partido") == true
        if (recargar) {
            vm.cargarDatos()
            navController.previousBackStackEntry?.arguments?.remove("reload_partido")
        }
    }
    LaunchedEffect(eliminado) {
        if (eliminado) {
            navController.navigate("partido") {
                popUpTo("partido") { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        topBar = {
            PartidoTopBar(
                partidoId = partidoId,
                navController = navController,
                onDelete = { showDeleteDialog = true }
            )
        }
    ) { innerPadding ->
        VisualizarPartidoContent(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState,
            vm = vm
        )
        if (showDeleteDialog) {
            EliminarPartidoDialog(
                onDismiss = { showDeleteDialog = false },
                onConfirm = {
                    showDeleteDialog = false
                    vm.eliminarPartido()
                }
            )
        }
    }
}
