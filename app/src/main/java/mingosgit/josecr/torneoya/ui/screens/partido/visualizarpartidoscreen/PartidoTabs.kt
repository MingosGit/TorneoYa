package mingosgit.josecr.torneoya.ui.screens.partido.visualizarpartidoscreen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mingosgit.josecr.torneoya.viewmodel.partido.VisualizarPartidoUiState
import mingosgit.josecr.torneoya.viewmodel.partido.VisualizarPartidoViewModel
import mingosgit.josecr.torneoya.repository.EventoRepository
import mingosgit.josecr.torneoya.repository.JugadorRepository
import mingosgit.josecr.torneoya.repository.EquipoRepository

@Composable
fun PartidoTabs(
    uiState: VisualizarPartidoUiState,
    vm: VisualizarPartidoViewModel,
    usuarioId: Long,
    eventoRepository: EventoRepository,
    jugadorRepository: JugadorRepository,
    equipoRepository: EquipoRepository,
    partidoId: Long
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Jugadores", "Eventos", "Comentarios", "Encuestas")

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
        0 -> PartidoTabJugadores(uiState)
        1 -> PartidoTabEventos(
            partidoId = partidoId,
            eventoRepository = eventoRepository,
            jugadorRepository = jugadorRepository,
            equipoRepository = equipoRepository
        )
        2 -> PartidoTabComentarios(vm)
        3 -> PartidoTabEncuestas(vm, usuarioId)
    }
}
