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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.firebase.PartidoFirebase
import mingosgit.josecr.torneoya.data.firebase.PartidoFirebaseRepository
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    navController: NavController,
    partidoRepo: PartidoFirebaseRepository = remember { PartidoFirebaseRepository() }
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var proximoPartido by remember { mutableStateOf<PartidoFirebase?>(null) }
    var cargandoProx by remember { mutableStateOf(true) }

    // Cargar próximo partido online al entrar (si hay usuario logueado)
    LaunchedEffect(userUid) {
        cargandoProx = true
        if (userUid.isNotBlank()) {
            val partidos = partidoRepo.listarPartidosPorUsuario(userUid)
                .filter { it.estado == "PREVIA" }
                .sortedBy { it.fecha + " " + it.horaInicio }
            proximoPartido = partidos.firstOrNull()
        }
        cargandoProx = false
    }

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
                horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally)
            ) {
                QuickAccessButton(
                    icon = Icons.Filled.Person,
                    label = "Mi perfil"
                ) { navController.navigate("usuario") }

                QuickAccessButton(
                    icon = Icons.Filled.SportsSoccer,
                    label = "Partidos Online"
                ) { navController.navigate("partido_online") }
            }

            Spacer(Modifier.height(29.dp))

            // Próximo partido online
            if (cargandoProx) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF296DFF), strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                }
            } else {
                proximoPartido?.let { partido ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(7.dp, RoundedCornerShape(18.dp)),
                        color = Color(0xFF20243B),
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
                                text = "${partido.fecha}  |  ${partido.horaInicio}",
                                color = Color(0xFFF7F7FF),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "${partido.nombresManualEquipoA.joinToString(" / ")}  VS  ${partido.nombresManualEquipoB.joinToString(" / ")}",
                                color = Color(0xFFB7B7D1),
                                fontSize = 15.sp,
                                maxLines = 1
                            )
                            Spacer(Modifier.height(7.dp))
                            Button(
                                onClick = {
                                    navController.navigate("visualizar_partido_online/${partido.uid}")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF296DFF)),
                                shape = RoundedCornerShape(13.dp),
                                modifier = Modifier.height(37.dp)
                            ) {
                                Text("Ver partido", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(29.dp))

            // Tarjeta bienvenida
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF20243B),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(9.dp, RoundedCornerShape(24.dp))
                ) {
                    Column(
                        Modifier
                            .padding(28.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "¡Bienvenido a TorneoYa!",
                            color = Color(0xFFF7F7FF),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "Gestiona tus partidos y amigos desde este panel. Explora todas las opciones en el menú inferior.",
                            color = Color(0xFFB7B7D1),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickAccessButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(15.dp))
            .background(Color(0xFF23273D))
            .clickable { onClick() }
            .padding(horizontal = 19.dp, vertical = 13.dp)
    ) {
        Icon(icon, contentDescription = label, tint = Color(0xFF296DFF), modifier = Modifier.size(28.dp))
        Spacer(Modifier.height(6.dp))
        Text(label, color = Color(0xFFB7B7D1), fontSize = 13.sp, fontWeight = FontWeight.Medium)
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
