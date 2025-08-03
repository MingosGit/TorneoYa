package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
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
fun PartidoTabJugadoresOnline(uiState: VisualizarPartidoOnlineUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        EquipoColumn(
            nombreEquipo = uiState.nombreEquipoA,
            jugadores = uiState.jugadoresEquipoA,
            borderBrush = Brush.horizontalGradient(
                listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
            ),
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        )
        EquipoColumn(
            nombreEquipo = uiState.nombreEquipoB,
            jugadores = uiState.jugadoresEquipoB,
            borderBrush = Brush.horizontalGradient(
                listOf(TorneoYaPalette.accent, TorneoYaPalette.violet)
            ),
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )
    }
}

@Composable
private fun EquipoColumn(
    nombreEquipo: String,
    jugadores: List<String>,
    borderBrush: Brush,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = nombreEquipo,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TorneoYaPalette.violet,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
        )
        Divider(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .height(2.dp)
                .background(TorneoYaPalette.violet)
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(jugadores) { jugador ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp, horizontal = 10.dp)
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
                            brush = borderBrush,
                            shape = RoundedCornerShape(13.dp)
                        )
                        .padding(vertical = 9.dp, horizontal = 2.dp)
                ) {
                    Text(
                        text = jugador,
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            if (jugadores.isEmpty()) {
                item {
                    Text(
                        text = "Sin jugadores asignados",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
