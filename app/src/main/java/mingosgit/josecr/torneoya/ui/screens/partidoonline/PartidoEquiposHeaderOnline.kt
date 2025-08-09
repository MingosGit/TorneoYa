package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mingosgit.josecr.torneoya.viewmodel.partidoonline.VisualizarPartidoOnlineUiState

@Composable
fun PartidoEquiposHeaderOnline(uiState: VisualizarPartidoOnlineUiState) {
    val cs = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp, top = 10.dp, start = 12.dp, end = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Equipo A
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(end = 6.dp)
                .background(
                    brush = Brush.radialGradient(
                        listOf(cs.primary.copy(alpha = 0.21f), cs.background.copy(alpha = 0f))
                    ),
                    shape = CircleShape
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(listOf(cs.primary, cs.secondary)),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(cs.surfaceVariant, cs.surface)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(vertical = 11.dp, horizontal = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.nombreEquipoA,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Black,
                    color = cs.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .fillMaxWidth()
                )
            }
        }
        // VS
        Text(
            text = "VS",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = cs.secondary,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .align(Alignment.CenterVertically),
            textAlign = TextAlign.Center
        )
        // Equipo B
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 6.dp)
                .background(
                    brush = Brush.radialGradient(
                        listOf(cs.tertiary.copy(alpha = 0.18f), cs.background.copy(alpha = 0f))
                    ),
                    shape = CircleShape
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(listOf(cs.tertiary, cs.secondary)),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(cs.surface, cs.surfaceVariant)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(vertical = 11.dp, horizontal = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.nombreEquipoB,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Black,
                    color = cs.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .fillMaxWidth()
                )
            }
        }
    }
}
