package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.viewmodel.partidoonline.VisualizarPartidoOnlineUiState

@Composable
fun PartidoGolesHeaderOnline(
    uiState: VisualizarPartidoOnlineUiState,
    onRecargarGoles: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Marcador equipo A
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
                .shadow(2.dp, RoundedCornerShape(13.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color(0xFF23273D),
                            Color(0xFF1C1D25)
                        )
                    ),
                    shape = RoundedCornerShape(13.dp)
                )
                .border(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(
                        listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                    ),
                    shape = RoundedCornerShape(13.dp)
                )
                .padding(vertical = 9.dp, horizontal = 2.dp)
        ) {
            Text(
                text = "${uiState.golesEquipoA}",
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Text(
            text = "-",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp),
            textAlign = TextAlign.Center
        )
        // Marcador equipo B
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
                .shadow(2.dp, RoundedCornerShape(13.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color(0xFF23273D),
                            Color(0xFF1C1D25)
                        )
                    ),
                    shape = RoundedCornerShape(13.dp)
                )
                .border(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(
                        listOf(TorneoYaPalette.accent, TorneoYaPalette.violet)
                    ),
                    shape = RoundedCornerShape(13.dp)
                )
                .padding(vertical = 9.dp, horizontal = 2.dp)
        ) {
            Text(
                text = "${uiState.golesEquipoB}",
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (onRecargarGoles != null) {
            IconButton(
                onClick = { onRecargarGoles() },
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Recargar goles"
                )
            }
        }
    }
}
