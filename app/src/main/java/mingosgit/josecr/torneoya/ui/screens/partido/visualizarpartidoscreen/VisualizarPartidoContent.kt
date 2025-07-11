package mingosgit.josecr.torneoya.ui.screens.partido.visualizarpartidoscreen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mingosgit.josecr.torneoya.viewmodel.partido.VisualizarPartidoViewModel
import mingosgit.josecr.torneoya.viewmodel.partido.VisualizarPartidoUiState
import mingosgit.josecr.torneoya.repository.EventoRepository
import mingosgit.josecr.torneoya.repository.JugadorRepository
import mingosgit.josecr.torneoya.repository.EquipoRepository

@Composable
fun VisualizarPartidoContent(
    modifier: Modifier = Modifier,
    uiState: VisualizarPartidoUiState,
    vm: VisualizarPartidoViewModel,
    usuarioId: Long,
    eventoRepository: EventoRepository,
    jugadorRepository: JugadorRepository,
    equipoRepository: EquipoRepository,
    partidoId: Long
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        PartidoEquiposHeader(uiState)
        PartidoGolesHeader(uiState)
        PartidoEstadoBanner(uiState)
        Spacer(modifier = Modifier.height(16.dp))
        PartidoTabs(
            uiState = uiState,
            vm = vm,
            usuarioId = usuarioId,
            eventoRepository = eventoRepository,
            jugadorRepository = jugadorRepository,
            equipoRepository = equipoRepository,
            partidoId = partidoId
        )
    }
}
