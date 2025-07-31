package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.viewmodel.partidoonline.VisualizarPartidoOnlineUiState
import mingosgit.josecr.torneoya.viewmodel.partidoonline.VisualizarPartidoOnlineViewModel

@Composable
fun PartidoTabsOnline(
    uiState: VisualizarPartidoOnlineUiState,
    vm: VisualizarPartidoOnlineViewModel,
    usuarioUid: String,
    partidoUid: String,
    golesReloadKey: Int = 0
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Jugadores", "Eventos", "Comentarios", "Encuestas")
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var reloadEventos by remember { mutableStateOf(0) }

    ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = Modifier.fillMaxWidth(),
        edgePadding = 0.dp
    ) {
        tabTitles.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = {
                    isLoading = true
                    selectedTabIndex = index
                    scope.launch {
                        when (index) {
                            0 -> vm.cargarDatos(usuarioUid)
                            1 -> { reloadEventos++ }
                            2 -> vm.cargarComentariosEncuestas(usuarioUid)
                            3 -> vm.cargarComentariosEncuestas(usuarioUid)
                        }
                        kotlinx.coroutines.delay(500)
                        isLoading = false
                    }
                },
                text = { Text(title, fontSize = 16.sp) }
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))

    if (isLoading) {
        Box(
            Modifier.fillMaxWidth().height(200.dp),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
    } else {
        when (selectedTabIndex) {
            0 -> PartidoTabJugadoresOnline(uiState)
            1 -> PartidoTabEventosOnline(partidoUid, uiState, reloadKey = reloadEventos + golesReloadKey)
            2 -> PartidoTabComentariosOnline(vm, usuarioUid)
            3 -> PartidoTabEncuestasOnline(vm, usuarioUid)
        }
    }
}
