package mingosgit.josecr.torneoya.ui.screens.partidoonline.visualizacion.visualizarPartido

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mingosgit.josecr.torneoya.R
import mingosgit.josecr.torneoya.viewmodel.partidoonline.VisualizarPartidoOnlineUiState

/**
 * Banner superior que muestra el estado del partido online.
 * - Cambia color y borde según estado (Finalizado, Jugando, Descanso, Previa)
 * - A la derecha muestra el minuto actual o texto de descanso
 */
@Composable
fun PartidoEstadoBannerOnline(uiState: VisualizarPartidoOnlineUiState) {
    val cs = MaterialTheme.colorScheme

    // Pincel de borde y color de texto según estado
    val (borderBrush, textColor) = when (uiState.estado) {
        "Finalizado" -> Pair(
            Brush.horizontalGradient(listOf(cs.error, cs.secondary)),
            cs.error
        )
        "Jugando" -> Pair(
            Brush.horizontalGradient(listOf(cs.tertiary, cs.secondary)),
            cs.tertiary
        )
        "Descanso" -> Pair(
            Brush.horizontalGradient(listOf(cs.primary, cs.secondary)),
            cs.primary
        )
        "Previa" -> Pair(
            Brush.horizontalGradient(listOf(cs.primary, cs.secondary)),
            cs.primary
        )
        else -> Pair(
            Brush.horizontalGradient(listOf(cs.secondary, cs.secondary)),
            cs.secondary
        )
    }

    // Contenedor horizontal con estado y minuto/texto adicional
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
        // Texto izquierda: "Estado: X"
        Text(
            text = stringResource(R.string.parequban_estado, uiState.estado),
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Start
        )
        // Texto derecha: minuto actual si jugando, "Descanso" si procede
        Text(
            text = when (uiState.estado) {
                "Jugando" -> "${uiState.minutoActual}"
                "Descanso" -> stringResource(R.string.parequban_descanso)
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
