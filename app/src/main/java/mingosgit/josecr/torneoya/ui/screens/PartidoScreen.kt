package mingosgit.josecr.torneoya.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

@Composable
fun PartidoScreen() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Crear Partido",
            fontSize = 28.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
