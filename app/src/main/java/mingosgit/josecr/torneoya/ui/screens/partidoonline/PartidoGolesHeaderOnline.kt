package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
            .padding(vertical = 6.dp, horizontal = 14.dp), // Agrega padding lateral aquí
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Marcador equipo A
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(end = 6.dp)
                .shadow(2.dp, RoundedCornerShape(14.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFF191A23), Color(0xFF23273D))
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
                .border(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(
                        listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(vertical = 4.dp, horizontal = 6.dp)
        ) {
            Text(
                text = "${uiState.golesEquipoA}",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Botón recargar en el centro
        if (onRecargarGoles != null) {
            IconButton(
                onClick = { onRecargarGoles() },
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(34.dp)
                    .background(
                        color = Color(0xFF23273D),
                        shape = RoundedCornerShape(50)
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(
                            listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                        ),
                        shape = RoundedCornerShape(50)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Recargar goles",
                    tint = Color(0xFF8F5CFF),
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Spacer(Modifier.width(12.dp))
        }

        // Marcador equipo B
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 6.dp)
                .shadow(2.dp, RoundedCornerShape(14.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFF23273D), Color(0xFF191A23))
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
                .border(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(
                        listOf(TorneoYaPalette.accent, TorneoYaPalette.violet)
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(vertical = 4.dp, horizontal = 6.dp)
        ) {
            Text(
                text = "${uiState.golesEquipoB}",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
