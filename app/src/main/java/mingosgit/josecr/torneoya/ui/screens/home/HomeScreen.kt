package mingosgit.josecr.torneoya.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.firebase.PartidoFirebase
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette

data class HomeProximoPartidoUi(
    val partido: PartidoFirebase,
    val nombreEquipoA: String,
    val nombreEquipoB: String
)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val proximoPartidoUi by viewModel.proximoPartidoUi.collectAsState()
    val cargandoProx by viewModel.cargandoProx.collectAsState()
    val scope = rememberCoroutineScope()

    val modernBackground = Brush.verticalGradient(
        0.0f to Color(0xFF1B1D29),
        0.28f to Color(0xFF212442),
        0.58f to Color(0xFF191A23),
        1.0f to Color(0xFF14151B)
    )

    var isLoading by remember { mutableStateOf(true) }
    var loadingTimeoutReached by remember { mutableStateOf(false) }
    var showNoSesionScreen by remember { mutableStateOf(false) }

    // Lanzamos la animación de carga por máx 2 segundos o hasta que se cargue el usuario
    LaunchedEffect(uiState.nombreUsuario) {
        isLoading = true
        loadingTimeoutReached = false
        showNoSesionScreen = false

        val sesionActiva = uiState.nombreUsuario.isNotBlank() && uiState.nombreUsuario != "Usuario"
        // Si no está la sesión activa, pero aún podría cargarse, espera 2 segundos
        val delayJob = launch {
            delay(2000)
            loadingTimeoutReached = true
        }
        // Espera hasta que llegue nombre válido o timeout
        while (!sesionActiva && !loadingTimeoutReached) {
            delay(100)
        }
        isLoading = false
        showNoSesionScreen = !sesionActiva
        delayJob.cancel()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = modernBackground)
    ) {
        // ----------- BOTON NOTIFICACIONES ----------- //
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, end = 18.dp)
                .align(Alignment.TopEnd)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(
                            listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                        ),
                        shape = CircleShape
                    )
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                        )
                    )
                    .clickable { navController.navigate("notificaciones") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = "Notificaciones",
                    tint = Color(0xFF8F5CFF),
                    modifier = Modifier.size(25.dp)
                )
            }
        }

        // ---------------- ANIMACIÓN DE CARGA ------------------- //
        if (isLoading) {
            Box(
                Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF296DFF),
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(18.dp))
                    Text(
                        text = "Cargando tu cuenta...",
                        color = Color(0xFFB7B7D1),
                        fontSize = 17.sp
                    )
                }
            }
            return
        }

        // ----------------- PANTALLA NO SESIÓN ----------------- //
        if (showNoSesionScreen) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 26.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF296DFF).copy(alpha = 0.13f),
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
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Organiza y disfruta tus partidos.\n\nInicia sesión o crea tu cuenta para empezar.",
                    fontSize = 16.sp,
                    color = Color(0xFFB7B7D1),
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    fontWeight = FontWeight.Normal
                )
                Spacer(Modifier.height(32.dp))

                // BOTÓN INICIAR SESIÓN
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .border(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(
                                listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                            ),
                            shape = RoundedCornerShape(15.dp)
                        )
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                            )
                        )
                        .clickable { navController.navigate("login") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Iniciar sesión",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(Modifier.height(11.dp))

                // BOTÓN CREAR CUENTA
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .border(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(
                                listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                            ),
                            shape = RoundedCornerShape(15.dp)
                        )
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                            )
                        )
                        .clickable { navController.navigate("register") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Crear cuenta",
                        color = TorneoYaPalette.blue,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }

                Spacer(Modifier.height(26.dp))
                Text(
                    text = "¿Prefieres una cuenta local?\nAccede desde ajustes de Usuario.",
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

        // ------------ PANTALLA NORMAL -------------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF22243A),
                    shadowElevation = 14.dp,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Perfil",
                        tint = Color(0xFFFFB531),
                        modifier = Modifier.padding(14.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = "¡Hola, ${uiState.nombreUsuario}!",
                        fontSize = 27.sp,
                        color = Color(0xFFF7F7FF),
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Resumen de tu actividad",
                        fontSize = 15.sp,
                        color = Color(0xFFB7B7D1),
                        fontWeight = FontWeight.Normal
                    )
                }
            }
            Spacer(Modifier.height(29.dp))
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
            Spacer(Modifier.height(31.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally)
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
            Spacer(Modifier.height(27.dp))
            if (cargandoProx) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF296DFF),
                        strokeWidth = 2.2.dp,
                        modifier = Modifier.size(26.dp)
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
                                .clip(RoundedCornerShape(17.dp))
                                .border(
                                    width = 2.dp,
                                    brush = Brush.horizontalGradient(
                                        listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                                    ),
                                    shape = RoundedCornerShape(17.dp)
                                )
                                .background(Color(0xFF1B1E2E)),
                            color = Color.Transparent,
                            shadowElevation = 0.dp,
                            shape = RoundedCornerShape(17.dp)
                        ) {
                            Column(
                                Modifier
                                    .padding(vertical = 18.dp, horizontal = 19.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "Próximo partido online",
                                    color = Color(0xFF8F5CFF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(Modifier.height(5.dp))
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
                                Spacer(Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(
                                            width = 2.dp,
                                            brush = Brush.horizontalGradient(
                                                listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                                            )
                                        )
                                        .clickable {
                                            navController.navigate("visualizar_partido_online/${partidoUi.partido.uid}")
                                        }
                                        .height(38.dp)
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
                            .clip(RoundedCornerShape(17.dp))
                            .border(
                                width = 2.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(TorneoYaPalette.accent, TorneoYaPalette.violet)
                                ),
                                shape = RoundedCornerShape(17.dp)
                            )
                            .background(Color(0xFF20243B))
                            .padding(vertical = 24.dp, horizontal = 15.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "¡Sin próximos partidos online!",
                            color = Color(0xFFB7B7D1),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(7.dp))
                        Text(
                            text = "Crea uno o únete a una partida para no perderte ningún gol.",
                            color = Color(0xFF8F5CFF),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        Spacer(Modifier.height(17.dp))
                        Row {
                            Button(
                                onClick = { navController.navigate("partido_online") },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF296DFF)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Buscar partidos", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(15.dp))
                            OutlinedButton(
                                onClick = { navController.navigate("crear_partido_online") },
                                shape = RoundedCornerShape(10.dp),
                                border = ButtonDefaults.outlinedButtonBorder,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Crear uno", color = Color(0xFF8F5CFF), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

// COMPONENTES REUTILIZABLES

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
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFF296DFF), TorneoYaPalette.violet)
                ),
                shape = RoundedCornerShape(17.dp)
            )
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                )
            )
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
                .border(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(
                        if (label == "Partidos")
                            listOf(Color(0xFF296DFF), TorneoYaPalette.violet)
                        else
                            listOf(Color(0xFFFF7675), TorneoYaPalette.violet)
                    ),
                    shape = CircleShape
                )
                .background(
                    Brush.radialGradient(
                        colors = listOf(color.copy(alpha = 0.22f), Color.Transparent),
                        radius = 46f
                    )
                ),
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
