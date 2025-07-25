package mingosgit.josecr.torneoya.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.recargarDatos()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A2980), Color(0xFF26D0CE))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 28.dp)
        ) {
            // Avatar y bienvenida
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Perfil",
                        tint = Color(0xFFFFC300),
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "¡Hola, ${uiState.nombreUsuario}!",
                        fontSize = 27.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Resumen de tu actividad",
                        fontSize = 16.sp,
                        color = Color(0xFFEEEEEE),
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Spacer(Modifier.height(30.dp))

            // Stats PRO
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCircle(
                    icon = Icons.Filled.SportsSoccer,
                    label = "Partidos",
                    value = uiState.partidosTotales,
                    color = Color(0xFF00B894)
                )
                StatCircle(
                    icon = Icons.Filled.Group,
                    label = "Equipos",
                    value = uiState.equiposTotales,
                    color = Color(0xFF0984E3)
                )
                StatCircle(
                    icon = Icons.Filled.Person,
                    label = "Jugadores",
                    value = uiState.jugadoresTotales,
                    color = Color(0xFFFFB300)
                )
                StatCircle(
                    icon = Icons.Filled.Star,
                    label = "Amigos",
                    value = uiState.amigosTotales,
                    color = Color(0xFFFF7675)
                )
            }

            Spacer(Modifier.height(34.dp))

            // Tarjeta con sombra sutil
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut()
            ) {
                Card(
                    shape = RoundedCornerShape(26.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(26.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFBF6F6))
                ) {
                    Column(
                        Modifier
                            .padding(30.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "¡Bienvenido a TorneoYa!",
                            color = Color(0xFF2D3436),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "Gestiona tus partidos, equipos y amigos desde este panel. Explora todas las opciones en el menú inferior.",
                            color = Color(0xFF636E72),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCircle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(84.dp)
    ) {
        Box(
            Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = value.toString(),
            fontSize = 21.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}
