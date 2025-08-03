package mingosgit.josecr.torneoya.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import mingosgit.josecr.torneoya.data.firebase.NotificacionFirebase
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette

@Composable
fun NotificacionesScreen(
    usuarioUid: String,
    viewModel: NotificacionesViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotificacionesViewModel(usuarioUid) as T
        }
    })
) {
    val notificaciones by viewModel.notificaciones.collectAsState()
    val cargando by viewModel.cargando.collectAsState()

    val background = Brush.verticalGradient(
        0.0f to Color(0xFF1B1D29),
        0.28f to Color(0xFF212442),
        0.58f to Color(0xFF191A23),
        1.0f to Color(0xFF14151B)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(horizontal = 18.dp, vertical = 18.dp)
    ) {
        when {
            cargando -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = Color(0xFF8F5CFF),
                        strokeWidth = 2.3.dp,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
            notificaciones.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .border(
                                    width = 2.5.dp,
                                    brush = Brush.horizontalGradient(
                                        listOf(Color(0xFF296DFF), TorneoYaPalette.violet)
                                    ),
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MarkEmailRead,
                                contentDescription = "Sin notificaciones",
                                tint = Color(0xFF8F5CFF),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(Modifier.height(18.dp))
                        Text(
                            text = "Sin notificaciones",
                            color = Color(0xFF8F5CFF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 21.sp
                        )
                        Spacer(Modifier.height(7.dp))
                        Text(
                            text = "¡Estás al día! Cuando tengas novedades,\naparecerán aquí.",
                            color = Color(0xFFB7B7D1),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                    }
                }
            }
            else -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    notificaciones.forEach { noti ->
                        NotificacionCard(noti)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificacionCard(noti: NotificacionFirebase) {
    val color = when (noti.tipo) {
        "parche" -> Color(0xFF296DFF)
        "infraccion" -> Color(0xFFFF7675)
        else -> Color(0xFF8F5CFF)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    listOf(color, TorneoYaPalette.violet)
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                )
            )
            .padding(vertical = 18.dp, horizontal = 16.dp)
    ) {
        Column {
            Text(
                text = noti.titulo,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                maxLines = 2
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = noti.mensaje,
                color = Color(0xFFB7B7D1),
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            )
            if (noti.fechaHora.isNotBlank()) {
                Spacer(Modifier.height(7.dp))
                Text(
                    text = noti.fechaHora.replace('T', ' ').substring(0, 16),
                    color = Color(0xFF8F5CFF),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
