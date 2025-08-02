package mingosgit.josecr.torneoya.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.data.firebase.PartidoFirebase

data class HomeProximoPartidoUi(
    val partido: PartidoFirebase,
    val nombreEquipoA: String,
    val nombreEquipoB: String
)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val proximoPartidoUi by viewModel.proximoPartidoUi.collectAsState()
    val cargandoProx by viewModel.cargandoProx.collectAsState()

    val modernBackground = Brush.verticalGradient(
        0.0f to Color(0xFF181B26),
        0.25f to Color(0xFF22263B),
        0.6f to Color(0xFF1A1E29),
        1.0f to Color(0xFF161622)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = modernBackground)
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
                    color = Color(0xFF2A2A3A),
                    shadowElevation = 10.dp,
                    modifier = Modifier.size(54.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Perfil",
                        tint = Color(0xFFFFB531),
                        modifier = Modifier.padding(13.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = "¡Hola, ${uiState.nombreUsuario}!",
                        fontSize = 26.sp,
                        color = Color(0xFFF7F7FF),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Resumen de tu actividad",
                        fontSize = 15.sp,
                        color = Color(0xFFB7B7D1),
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Stats SOLO: Partidos y Amigos
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCircle(
                    icon = Icons.Filled.SportsSoccer,
                    label = "Partidos",
                    value = uiState.partidosTotales,
                    color = Color(0xFF296DFF)
                )
                StatCircle(
                    icon = Icons.Filled.Group,
                    label = "Amigos",
                    value = uiState.amigosTotales,
                    color = Color(0xFFFF7675)
                )
            }

            Spacer(Modifier.height(34.dp))

            // Accesos rápidos
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
            ) {
                QuickAccessButton(
                    icon = Icons.Filled.Person,
                    label = "Mi perfil",
                    modifier = Modifier.weight(1f)
                ) { navController.navigate("usuario") }

                QuickAccessButton(
                    icon = Icons.Filled.SportsSoccer,
                    label = "Partidos Online",
                    modifier = Modifier.weight(1f)
                ) { navController.navigate("partido_online") }
            }

            Spacer(Modifier.height(32.dp))

            // Próximo partido online
            if (cargandoProx) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF296DFF),
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                proximoPartidoUi?.let { partidoUi ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF20243B)),
                        color = Color.Transparent,
                        shadowElevation = 0.dp,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(
                            Modifier
                                .padding(vertical = 21.dp, horizontal = 20.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Próximo partido online",
                                color = Color(0xFF8F5CFF),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Spacer(Modifier.height(7.dp))
                            Text(
                                text = "${partidoUi.partido.fecha}  |  ${partidoUi.partido.horaInicio}",
                                color = Color(0xFFF7F7FF),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(7.dp))

                            // Mostrar nombres de los equipos desde el ViewModel
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = partidoUi.nombreEquipoA,
                                    color = Color(0xFFB7B7D1),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "   VS   ",
                                    color = Color(0xFF8F5CFF),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = partidoUi.nombreEquipoB,
                                    color = Color(0xFFB7B7D1),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(Modifier.height(13.dp))

                            // Botón totalmente personalizado con estilo plano (flat)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF23273D))
                                    .clickable {
                                        navController.navigate("visualizar_partido_online/${partidoUi.partido.uid}")
                                    }
                                    .height(39.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Ver partido",
                                    color = Color(0xFF8F5CFF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
fun QuickAccessButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(90.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(Color(0xFF23273D))
            .clickable { onClick() }
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = Color(0xFF296DFF), modifier = Modifier.size(30.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, color = Color(0xFFB7B7D1), fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
            .width(104.dp)
    ) {
        Box(
            Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.19f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(31.dp))
        }
        Spacer(Modifier.height(9.dp))
        Text(
            text = value.toString(),
            fontSize = 23.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFFF7F7FF),
            fontWeight = FontWeight.Medium
        )
    }
}
