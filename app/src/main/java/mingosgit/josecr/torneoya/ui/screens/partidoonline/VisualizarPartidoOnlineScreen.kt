package mingosgit.josecr.torneoya.ui.screens.partidoonline

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showCopiedMessage by remember { mutableStateOf(false) }
    val eliminado by vm.eliminado.collectAsState()
    val uiState by vm.uiState.collectAsState()

    LaunchedEffect(partidoUid) { vm.cargarDatos(usuarioUid) }

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
                    IconButton(onClick = {
                        navController.navigate("administrar_partido_online")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Administrar Partido"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            VisualizarPartidoOnlineContent(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                vm = vm,
                usuarioUid = usuarioUid,
                partidoUid = partidoUid
            )

            if (showCopiedMessage) {
                LaunchedEffect(showCopiedMessage) {
                    if (showCopiedMessage) {
                        scope.launch {
                            delay(2000)
                            showCopiedMessage = false
                        }
                    }
                }
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Text("UID copiado al portapapeles")
                }
            }
        }
    }
}
