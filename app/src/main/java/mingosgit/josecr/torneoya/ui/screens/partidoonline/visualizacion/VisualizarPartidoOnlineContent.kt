package mingosgit.josecr.torneoya.ui.screens.partidoonline.visualizacion

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mingosgit.josecr.torneoya.ui.screens.partidoonline.visualizacion.visualizarPartido.PartidoEquiposHeaderOnline
import mingosgit.josecr.torneoya.ui.screens.partidoonline.visualizacion.visualizarPartido.PartidoEstadoBannerOnline
import mingosgit.josecr.torneoya.ui.screens.partidoonline.visualizacion.visualizarPartido.PartidoGolesHeaderOnline
import mingosgit.josecr.torneoya.ui.screens.partidoonline.visualizacion.visualizarPartido.PartidoTabsOnline
import mingosgit.josecr.torneoya.viewmodel.partidoonline.VisualizarPartidoOnlineUiState
import mingosgit.josecr.torneoya.viewmodel.partidoonline.VisualizarPartidoOnlineViewModel

@Composable
fun VisualizarPartidoOnlineContent(
    modifier: Modifier = Modifier,
    uiState: VisualizarPartidoOnlineUiState,
    vm: VisualizarPartidoOnlineViewModel,
    usuarioUid: String,
    partidoUid: String
) {
    var golesReloadKey by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(0.dp)
    ) {
        PartidoEquiposHeaderOnline(uiState)
        Spacer(Modifier.height(10.dp))
        PartidoGolesHeaderOnline(
            uiState = uiState,
            onRecargarGoles = { golesReloadKey++ }
        )
        Spacer(Modifier.height(10.dp))
        PartidoEstadoBannerOnline(uiState)
        Spacer(modifier = Modifier.height(20.dp))
        PartidoTabsOnline(
            uiState = uiState,
            vm = vm,
            usuarioUid = usuarioUid,
            partidoUid = partidoUid,
            golesReloadKey = golesReloadKey
        )
    }
}
