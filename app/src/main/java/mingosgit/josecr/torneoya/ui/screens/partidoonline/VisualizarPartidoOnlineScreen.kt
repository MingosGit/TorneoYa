package mingosgit.josecr.torneoya.ui.screens.partidoonline

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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

    val context = LocalContext.current
    var showCopiedMessage by remember { mutableStateOf(false) }

    LaunchedEffect(eliminado) {
        if (eliminado) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Visualizar Partido Online") },
                actions = {
                    IconButton(onClick = {
                        // Copiar UID al portapapeles
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Partido UID", partidoUid)
                        clipboard.setPrimaryClip(clip)
                        showCopiedMessage = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartir UID"
                        )
                    }
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
        if (showCopiedMessage) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { showCopiedMessage = false }) {
                        Text("OK")
                    }
                }
            ) { Text("UID copiado al portapapeles") }
        }
    }
}
