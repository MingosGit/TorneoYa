package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.viewmodel.partidoonline.VisualizarPartidoOnlineUiState

@Composable
fun PartidoEstadoBannerOnline(uiState: VisualizarPartidoOnlineUiState) {

    val (borderBrush, textColor) = when (uiState.estado) {
        "Finalizado" -> Pair(
            Brush.horizontalGradient(listOf(Color(0xFFD32F2F), TorneoYaPalette.violet)), // Rojo
            Color(0xFFD32F2F)
        )
        "Jugando" -> Pair(
            Brush.horizontalGradient(listOf(Color(0xFFFFD600), TorneoYaPalette.violet)), // Amarillo
            Color(0xFFFFA000)
        )
        "Descanso" -> Pair(
            Brush.horizontalGradient(listOf(Color(0xFF43A047), TorneoYaPalette.violet)), // Verde
            Color(0xFF43A047)
        )
        "Previa" -> Pair(
            Brush.horizontalGradient(listOf(Color(0xFF43A047), TorneoYaPalette.violet)), // Verde
            Color(0xFF43A047)
        )
        else -> Pair(
            Brush.horizontalGradient(listOf(TorneoYaPalette.violet, TorneoYaPalette.violet)),
            TorneoYaPalette.violet
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 10.dp)
            .border(
                width = 2.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(13.dp)
            )
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Estado: ${uiState.estado}",
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Start
        )
        Text(
            text =
                when (uiState.estado) {
                    "Jugando" -> "${uiState.minutoActual}"
                    "Descanso" -> "Descanso"
                    else -> ""
                },
            modifier = Modifier
                .padding(end = 16.dp)
                .weight(1f),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.End
        )
    }
}
