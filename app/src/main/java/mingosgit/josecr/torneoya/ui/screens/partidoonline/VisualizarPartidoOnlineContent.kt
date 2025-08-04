package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import mingosgit.josecr.torneoya.viewmodel.partidoonline.VisualizarPartidoOnlineUiState
import mingosgit.josecr.torneoya.viewmodel.partidoonline.VisualizarPartidoOnlineViewModel
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette

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
            .padding(0.dp) // padding ya lo aplicas desde el screen
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
