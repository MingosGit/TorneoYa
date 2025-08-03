package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
fun PartidoEquiposHeaderOnline(uiState: VisualizarPartidoOnlineUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp, top = 6.dp, start = 8.dp, end = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Equipo A
        Box(
            modifier = Modifier
                .weight(1f)
                .height(IntrinsicSize.Min)
                .padding(end = 4.dp)
                .border(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)),
                    shape = RoundedCornerShape(13.dp)
                )
                .background(
                    color = Color(0xFF23273D),
                    shape = RoundedCornerShape(13.dp)
                )
                .padding(vertical = 9.dp, horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = uiState.nombreEquipoA,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        // VS
        Text(
            text = "VS",
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TorneoYaPalette.violet,
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .align(Alignment.CenterVertically),
            textAlign = TextAlign.Center
        )
        // Equipo B
        Box(
            modifier = Modifier
                .weight(1f)
                .height(IntrinsicSize.Min)
                .padding(start = 4.dp)
                .border(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(listOf(TorneoYaPalette.accent, TorneoYaPalette.violet)),
                    shape = RoundedCornerShape(13.dp)
                )
                .background(
                    color = Color(0xFF23273D),
                    shape = RoundedCornerShape(13.dp)
                )
                .padding(vertical = 9.dp, horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = uiState.nombreEquipoB,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
