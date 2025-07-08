package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Puedes reutilizar tus componentes de header y banner pero adaptados a los datos online
        PartidoEquiposHeaderOnline(uiState)
        PartidoGolesHeaderOnline(uiState)
        PartidoEstadoBannerOnline(uiState)
        Spacer(modifier = Modifier.height(16.dp))
        PartidoTabsOnline(
            uiState = uiState,
            vm = vm,
            usuarioUid = usuarioUid,
            partidoUid = partidoUid
        )
    }
}
