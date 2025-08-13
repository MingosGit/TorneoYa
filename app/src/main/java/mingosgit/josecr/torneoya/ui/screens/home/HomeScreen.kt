package mingosgit.josecr.torneoya.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.R
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.ui.theme.mutedText
import mingosgit.josecr.torneoya.viewmodel.usuario.GlobalUserViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navController: NavController
) {
    val cs = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsState()
    val proximoPartidoUi by viewModel.proximoPartidoUi.collectAsState()
    val cargandoProx by viewModel.cargandoProx.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    val globalUserViewModel: GlobalUserViewModel = viewModel()
    LaunchedEffect(Unit) { globalUserViewModel.cargarNombreUsuarioOnlineSiSesionActiva() }

    val avatar by globalUserViewModel.avatar.collectAsState()
    val context = LocalContext.current
    val avatarRes = if (avatar != null)
        context.resources.getIdentifier("avatar_${avatar}", "drawable", context.packageName)
    else
        context.resources.getIdentifier("avatar_placeholder", "drawable", context.packageName)

    val modernBackground = TorneoYaPalette.backgroundGradient
    val gradientPrimarySecondary = remember(cs.primary, cs.secondary) {
        Brush.horizontalGradient(listOf(cs.primary, cs.secondary))
    }

    var isLoading by remember { mutableStateOf(true) }
    var loadingTimeoutReached by remember { mutableStateOf(false) }
    var showNoSesionScreen by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.nombreUsuario) {
        // La sesión ahora se basa en caché + online. Si hay nombre cacheado, es sesión activa.
        isLoading = true
        loadingTimeoutReached = false
        showNoSesionScreen = false

        val sesionActiva = uiState.nombreUsuario.isNotBlank()
        val delayJob = launch { delay(1200); loadingTimeoutReached = true }
        while (!sesionActiva && !loadingTimeoutReached) { delay(100) }
        isLoading = false
        showNoSesionScreen = !sesionActiva
        delayJob.cancel()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = modernBackground)
    ) {
        // Botón notificaciones con badge
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
                    .border(2.dp, gradientPrimarySecondary, CircleShape)
                    .background(Brush.horizontalGradient(listOf(cs.surfaceVariant, cs.surface)))
                    .clickable { navController.navigate("notificaciones") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = stringResource(id = R.string.gen_notificaciones_desc),
                    tint = cs.secondary,
                    modifier = Modifier.size(25.dp)
                )

                // Badge de no leídas
                AnimatedVisibility(visible = unreadCount > 0, enter = fadeIn(), exit = fadeOut()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-6).dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(cs.error),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = cs.primary, strokeWidth = 3.dp, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(18.dp))
                    Text(
                        text = stringResource(id = R.string.gen_cargando) + stringResource(id = R.string.gen_tu_cuenta),
                        color = MaterialTheme.colorScheme.mutedText,
                        fontSize = 17.sp
                    )
                }
            }
            return
        }

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
                    color = cs.primary.copy(alpha = 0.13f),
                    shadowElevation = 0.dp,
                    modifier = Modifier.size(85.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = stringResource(id = R.string.gen_notificaciones_desc), tint = cs.primary, modifier = Modifier.padding(22.dp))
                }
                Spacer(Modifier.height(18.dp))
                Text(stringResource(id = R.string.home_bienvenido_torneoya), fontSize = 27.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.mutedText)
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(id = R.string.home_organiza_disfruta),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.mutedText,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    fontWeight = FontWeight.Normal
                )
                Spacer(Modifier.height(32.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .border(2.dp, gradientPrimarySecondary, RoundedCornerShape(15.dp))
                        .background(Brush.horizontalGradient(listOf(cs.surfaceVariant, cs.surface)))
                        .clickable { navController.navigate("login") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(id = R.string.gen_iniciar_sesion), color = MaterialTheme.colorScheme.mutedText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(Modifier.height(11.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .border(2.dp, gradientPrimarySecondary, RoundedCornerShape(15.dp))
                        .background(Brush.horizontalGradient(listOf(cs.surfaceVariant, cs.surface)))
                        .clickable { navController.navigate("register") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(id = R.string.gen_crear_cuenta), color = MaterialTheme.colorScheme.mutedText, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
                Spacer(Modifier.height(26.dp))
                Text(
                    text = stringResource(id = R.string.home_cuenta_local),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.mutedText,
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
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Transparent,
                    shadowElevation = 0.dp,
                    modifier = Modifier
                        .size(56.dp)
                        .border(2.dp, gradientPrimarySecondary, CircleShape)
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(cs.surfaceVariant, cs.surface)))
                ) {
                    if (avatarRes != 0) {
                        Image(
                            painter = painterResource(id = avatarRes),
                            contentDescription = stringResource(id = R.string.gen_avatar_desc),
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("👤", fontSize = 32.sp)
                        }
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = stringResource(id = R.string.home_hola_usuario, uiState.nombreUsuario),
                        fontSize = 27.sp,
                        color = MaterialTheme.colorScheme.mutedText,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = stringResource(id = R.string.home_resumen_actividad),
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.mutedText,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Spacer(Modifier.height(29.dp))

            // Stats
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatCircle(Icons.Filled.SportsSoccer, stringResource(id = R.string.gen_partidos), uiState.partidosTotales, cs.primary)
                StatCircle(Icons.Filled.Group, stringResource(id = R.string.gen_amigos), uiState.amigosTotales, cs.error)
            }

            Spacer(Modifier.height(31.dp))

            // Quick actions
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally)
            ) {
                QuickAccessButton(Icons.Filled.Person, stringResource(id = R.string.gen_mi_perfil), Modifier.weight(1f), cs) {
                    navController.navigate("usuario")
                }
                QuickAccessButton(Icons.Filled.SportsSoccer, stringResource(id = R.string.gen_partidos_online), Modifier.weight(1f), cs) {
                    navController.navigate("partido_online")
                }
            }

            Spacer(Modifier.height(27.dp))

            // Próximo partido / Sin próximos partidos
            if (cargandoProx) {
                Box(Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = cs.primary, strokeWidth = 2.2.dp, modifier = Modifier.size(26.dp))
                }
            } else {
                AnimatedVisibility(visible = proximoPartidoUi != null, enter = fadeIn(), exit = fadeOut()) {
                    proximoPartidoUi?.let { partidoUi ->
                        val borderBrush = Brush.horizontalGradient(listOf(cs.primary, cs.secondary))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(17.dp))
                                .border(2.dp, borderBrush, RoundedCornerShape(17.dp))
                                .background(cs.surfaceVariant),
                            color = Color.Transparent,
                            shadowElevation = 0.dp,
                            shape = RoundedCornerShape(17.dp)
                        ) {
                            Column(
                                Modifier.padding(vertical = 18.dp, horizontal = 19.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = stringResource(id = R.string.home_proximo_partido_titulo),
                                    color = MaterialTheme.colorScheme.mutedText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(Modifier.height(5.dp))
                                Text(
                                    text = "${partidoUi.partido.fecha}  |  ${partidoUi.partido.horaInicio}",
                                    color = MaterialTheme.colorScheme.mutedText,
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
                                        color = MaterialTheme.colorScheme.mutedText,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "   VS   ",
                                        color = MaterialTheme.colorScheme.mutedText,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = partidoUi.nombreEquipoB,
                                        color = MaterialTheme.colorScheme.mutedText,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(2.dp, borderBrush, RoundedCornerShape(10.dp))
                                        .background(Brush.horizontalGradient(listOf(cs.surfaceVariant, cs.surface)))
                                        .clickable { navController.navigate("visualizar_partido_online/${partidoUi.partido.uid}") }
                                        .height(38.dp)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.gen_ver_partido),
                                        color = MaterialTheme.colorScheme.mutedText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = proximoPartidoUi == null, enter = fadeIn(), exit = fadeOut()) {
                    val borderBrush = Brush.horizontalGradient(listOf(cs.tertiary, cs.secondary))
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(17.dp))
                            .border(2.dp, borderBrush, RoundedCornerShape(17.dp))
                            .background(Brush.horizontalGradient(listOf(cs.surfaceVariant, cs.surface)))
                            .padding(vertical = 24.dp, horizontal = 15.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.home_sin_proximos_partidos),
                            color = MaterialTheme.colorScheme.mutedText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(7.dp))
                        Text(
                            text = stringResource(id = R.string.home_crea_une_partido),
                            color = MaterialTheme.colorScheme.mutedText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(17.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            QuickAccessButton(
                                icon = Icons.Filled.SportsSoccer,
                                label = stringResource(id = R.string.gen_buscar_partidos),
                                modifier = Modifier.weight(1f),
                                cs = cs
                            ) { navController.navigate("partido_online") }
                            QuickAccessButton(
                                icon = Icons.Filled.Star,
                                label = stringResource(id = R.string.gen_crear_uno),
                                modifier = Modifier.weight(1f),
                                cs = cs
                            ) { navController.navigate("crear_partido_online") }
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun QuickAccessButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    cs: ColorScheme,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(90.dp)
            .clip(RoundedCornerShape(17.dp))
            .border(2.dp, Brush.horizontalGradient(listOf(cs.primary, cs.secondary)), RoundedCornerShape(17.dp))
            .background(Brush.horizontalGradient(listOf(cs.surfaceVariant, cs.surface)))
            .clickable { onClick() }
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = label, tint = cs.primary, modifier = Modifier.size(30.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, color = MaterialTheme.colorScheme.mutedText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
    val cs = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(104.dp)) {
        Box(
            Modifier
                .size(56.dp)
                .clip(CircleShape)
                .border(2.dp, Brush.horizontalGradient(listOf(color, cs.secondary)), CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(color.copy(alpha = 0.22f), Color.Transparent),
                        radius = 46f
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(31.dp))
        }
        Spacer(Modifier.height(9.dp))
        Text(value.toString(), fontSize = 23.sp, color = color, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.mutedText, fontWeight = FontWeight.Medium)
    }
}
