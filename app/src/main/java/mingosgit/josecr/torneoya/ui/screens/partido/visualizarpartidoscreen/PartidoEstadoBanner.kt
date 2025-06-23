package mingosgit.josecr.torneoya.ui.screens.partido.visualizarpartidoscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mingosgit.josecr.torneoya.viewmodel.partido.VisualizarPartidoUiState

@Composable
fun PartidoEstadoBanner(uiState: VisualizarPartidoUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(
                when (uiState.estado) {
                    "Finalizado" -> Color(0xFFE0E0E0)
                    "Jugando" -> Color(0xFFB3E5FC)
                    "Descanso" -> Color(0xFFFFF59D)
                    else -> Color(0xFFEEEEEE)
                }
            )
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Estado: ${uiState.estado}",
            modifier = Modifier.padding(start = 16.dp),
            fontSize = 16.sp,
            color = Color.Black
        )
        Text(
            text = if (uiState.estado == "Jugando") "${uiState.minutoActual}" else if (uiState.estado == "Descanso") "Descanso" else "",
            modifier = Modifier.padding(end = 16.dp),
            fontSize = 16.sp,
            color = Color.Black
        )
    }
}
