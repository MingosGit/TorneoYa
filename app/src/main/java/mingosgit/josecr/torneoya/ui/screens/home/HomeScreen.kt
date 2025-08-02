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

    val sesionActiva = uiState.nombreUsuario.isNotBlank() && uiState.nombreUsuario != "Usuario"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = modernBackground)
    ) {
        if (!sesionActiva) {
            // PANTALLA DE BIENVENIDA PARA USUARIO SIN SESIÓN
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 26.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF296DFF).copy(alpha = 0.15f),
                    shadowElevation = 0.dp,
                    modifier = Modifier.size(85.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Logo",
                        tint = Color(0xFF296DFF),
                        modifier = Modifier.padding(22.dp)
                    )
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "Bienvenido a TorneoYa",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Desde aquí podrás crear y gestionar partidos con tus amigos.\n\nPara ello deberás iniciar sesión o crearte una cuenta.",
                    fontSize = 16.sp,
                    color = Color(0xFFB7B7D1),
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    fontWeight = FontWeight.Normal
                )
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = { navController.navigate("login") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(49.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF296DFF))
                ) {
                    Text("Iniciar sesión", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(Modifier.height(11.dp))
                OutlinedButton(
                    onClick = { navController.navigate("register") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(49.dp),
                    shape = RoundedCornerShape(15.dp),
                ) {
                    Text("Crear cuenta", color = Color(0xFF296DFF), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
                Spacer(Modifier.height(26.dp))
                Text(
                    text = "¿No quieres una cuenta online?\nPuedes acceder a tu cuenta local desde ajustes de Usuario.",
                    fontSize = 14.sp,
                    color = Color(0xFFB7B7D1),
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp),
                    lineHeight = 19.sp
                )
            }
            return
        }

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

            // Próximo partido online o mensaje motivador
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
                AnimatedVisibility(
                    visible = proximoPartidoUi != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
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
                AnimatedVisibility(
                    visible = proximoPartidoUi == null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF20243B))
                            .padding(vertical = 28.dp, horizontal = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "¡Sin próximos partidos online!",
                            color = Color(0xFFB7B7D1),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Crea uno o únete a una partida para no perderte ningún gol.",
                            color = Color(0xFF8F5CFF),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        Spacer(Modifier.height(20.dp))
                        Row {
                            Button(
                                onClick = { navController.navigate("partido_online") },
                                shape = RoundedCornerShape(11.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF296DFF)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Buscar partidos", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(16.dp))
                            OutlinedButton(
                                onClick = { navController.navigate("crear_partido_online") },
                                shape = RoundedCornerShape(11.dp),
                                border = ButtonDefaults.outlinedButtonBorder,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Crear uno", color = Color(0xFF8F5CFF), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}

// --------- COMPONENTES --------

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
