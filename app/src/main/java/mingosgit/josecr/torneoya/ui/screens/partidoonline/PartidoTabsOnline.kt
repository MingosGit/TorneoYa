package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mingosgit.josecr.torneoya.viewmodel.partidoonline.VisualizarPartidoOnlineUiState
import mingosgit.josecr.torneoya.viewmodel.partidoonline.VisualizarPartidoOnlineViewModel

@Composable
fun PartidoTabsOnline(
    uiState: VisualizarPartidoOnlineUiState,
    vm: VisualizarPartidoOnlineViewModel,
    usuarioUid: String,
    partidoUid: String
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Jugadores", "Comentarios", "Encuestas")

    ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = Modifier.fillMaxWidth(),
        edgePadding = 0.dp
    ) {
        tabTitles.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { selectedTabIndex = index },
                text = { Text(title, fontSize = 16.sp) }
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))

    when (selectedTabIndex) {
        0 -> PartidoTabJugadoresOnline(uiState)
        1 -> PartidoTabComentariosOnline(vm, usuarioUid)
        2 -> PartidoTabEncuestasOnline(vm, usuarioUid)
    }
}


