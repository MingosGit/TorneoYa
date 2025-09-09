package mingosgit.josecr.torneoya.ui.screens.usuario

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import mingosgit.josecr.torneoya.R
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.viewmodel.usuario.UsuarioLocalViewModel
import mingosgit.josecr.torneoya.viewmodel.usuario.GlobalUserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuarioScreen(
    usuarioLocalViewModel: UsuarioLocalViewModel,
    navController: NavController,
    globalUserViewModel: GlobalUserViewModel
) {
    // Efecto inicial: carga usuario local y nombre online si hay sesión
    LaunchedEffect(Unit) {
        usuarioLocalViewModel.cargarUsuario()
        globalUserViewModel.cargarNombreUsuarioOnlineSiSesionActiva()
    }

    // Estados observados de viewmodels
    val usuario by usuarioLocalViewModel.usuario.collectAsState()
    val nombreUsuarioOnline by globalUserViewModel.nombreUsuarioOnline.collectAsState()
    val sesionOnlineActiva by globalUserViewModel.sesionOnlineActiva.collectAsState(initial = false)
    val avatar by globalUserViewModel.avatar.collectAsState()

    // UI: diálogos y recorte de imagen
    var showDialog by remember { mutableStateOf(false) }
    var showCerrarSesionDialog by remember { mutableStateOf(false) }
    var cropUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    // --- NUEVO: GOLES, ASISTENCIAS Y PROMEDIO ---
    // Estados para estadísticas del jugador
    var goles by remember { mutableStateOf<Int?>(null) }
    var asistencias by remember { mutableStateOf<Int?>(null) }
    var partidosJugados by remember { mutableStateOf<Int?>(null) }
    var promedioGoles by remember { mutableStateOf<Double?>(null) }
    val context = LocalContext.current

    // Efecto que consulta Firestore cuando hay sesión: calcula goles/asistencias/partidos y promedio
    LaunchedEffect(sesionOnlineActiva, nombreUsuarioOnline) {
        if (sesionOnlineActiva) {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val uid = user.uid
                val db = FirebaseFirestore.getInstance()
                // Consulta goles del usuario por uid
                val golesSnapshot = db.collection("goleadores")
                    .whereEqualTo("jugadorUid", uid)
                    .get().await()
                goles = golesSnapshot.size()

                // Consulta asistencias del usuario por uid
                val asistenciasSnapshot = db.collection("goleadores")
                    .whereEqualTo("asistenciaJugadorUid", uid)
                    .get().await()
                asistencias = asistenciasSnapshot.size()

                // Calcula partidos distintos donde tuvo gol o asistencia
                val partidoGoles = golesSnapshot.documents.mapNotNull { it.getString("partidoUid") }
                val partidoAsistencias = asistenciasSnapshot.documents.mapNotNull { it.getString("partidoUid") }
                val partidosSet = (partidoGoles + partidoAsistencias).toSet()
                partidosJugados = partidosSet.size

                // Calcula promedio de goles por partido
                promedioGoles = if (partidosJugados ?: 0 > 0) {
                    (goles?.toDouble() ?: 0.0) / (partidosJugados?.toDouble() ?: 1.0)
                } else {
                    0.0
                }
            }
        } else {
            // Limpia estadísticas si no hay sesión
            goles = null
            asistencias = null
            partidosJugados = null
            promedioGoles = null
        }
    }

    // Launcher para seleccionar imagen de la galería
    val selectImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            cropUri = uri
        }
    }

    // Paleta y colores de la pantalla
    val modernBackground = TorneoYaPalette.backgroundGradient
    val blue = TorneoYaPalette.blue
    val violet = TorneoYaPalette.violet
    val accent = TorneoYaPalette.accent
    val lightText = TorneoYaPalette.textLight
    val mutedText = TorneoYaPalette.mutedText
    val rojo = Color(0xFFFF2D55)

    // Diálogo para confirmar cambio de foto (lanza selector)
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(id = R.string.usuario_cambiar_foto_perfil_title)) },
            text = { Text(stringResource(id = R.string.usuario_cambiar_foto_perfil_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    selectImageLauncher.launch("image/*")
                }) {
                    Text(stringResource(id = R.string.usuario_si))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(id = R.string.usuario_no))
                }
            }
        )
    }

    // Diálogo de confirmar cierre de sesión (custom)
    if (showCerrarSesionDialog) {
        CustomCerrarSesionDialog(
            onConfirm = {
                // Cierra sesión, cierra diálogo y navega a login limpiando backstack
                globalUserViewModel.cerrarSesionOnline()
                showCerrarSesionDialog = false
                navController.navigate("login") {
                    popUpTo("usuario") { inclusive = true }
                }
            },
            onDismiss = { showCerrarSesionDialog = false },
            blue = blue,
            violet = violet,
            rojo = rojo,
            accent = accent,
            background = Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surfaceVariant
            )),
            lightText = lightText,
            mutedText = mutedText
        )
    }

    // Si hay uri para recortar, muestra diálogo de recorte y guarda resultado en viewmodel local
    cropUri?.let { uriParaCropear ->
        CropImageDialog(
            uri = uriParaCropear,
            onDismiss = { cropUri = null },
            onCropDone = { croppedPath ->
                usuarioLocalViewModel.cambiarFotoPerfil(croppedPath)
                cropUri = null
            },
            navController = navController as NavHostController
        )
    }

    // Estructura principal de la pantalla
    Scaffold(
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(modernBackground)
                .padding(horizontal = 0.dp, vertical = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Fila superior: título, botón cerrar sesión (si hay), icono ajustes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Título de pantalla
                Text(
                    stringResource(id = R.string.usuario_perfil_title),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 6.dp)
                )
                // Botón cerrar sesión visible solo con sesión activa
                if (sesionOnlineActiva) {
                    Box(
                        modifier = Modifier
                            .height(42.dp)
                            .clip(CircleShape)
                            .border(
                                width = 2.dp,
                                brush = Brush.horizontalGradient(listOf(rojo, violet)),
                                shape = CircleShape
                            )
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.surface,
                                        MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            )
                            .clickable { showCerrarSesionDialog = true }
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.usuario_cerrar_sesion_button),
                            color = rojo,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(horizontal = 0.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                // Icono de ajustes: navega a pantalla "ajustes"
                IconButton(
                    onClick = { navController.navigate("ajustes") },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(
                                listOf(blue, violet)
                            ),
                            shape = CircleShape
                        )
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(id = R.string.usuario_ajustes_desc),
                        tint = Color(0xFF8F5CFF),
                        modifier = Modifier.size(25.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Relee avatar para pintar (ya observado arriba)
            val avatar by globalUserViewModel.avatar.collectAsState()

            // Box del avatar: círculo con borde y click para ir a pantalla de avatares si hay sesión
            Box(
                modifier = Modifier
                    .size(118.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(
                            listOf(blue, violet)
                        ),
                        shape = CircleShape
                    )
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    )
                    .let { baseModifier ->
                        if (sesionOnlineActiva) {
                            baseModifier.clickable { navController.navigate("avatar") }
                        } else {
                            baseModifier
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                val context = LocalContext.current
                // ResId del avatar elegido o placeholder si no hay
                val avatarRes = if (avatar != null)
                    context.resources.getIdentifier("avatar_${avatar}", "drawable", context.packageName)
                else
                    context.resources.getIdentifier("avatar_placeholder", "drawable", context.packageName)

                // Dibuja imagen del avatar o fallback emoji
                if (avatarRes != 0) {
                    Image(
                        painter = painterResource(id = avatarRes),
                        contentDescription = stringResource(id = R.string.usuario_avatar_desc),
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Text("👤", fontSize = 52.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Texto de saludo: personalizado si hay sesión, genérico si no
            if (sesionOnlineActiva) {
                Text(
                    text = if (!nombreUsuarioOnline.isNullOrBlank()) "${stringResource(id = R.string.usuario_hola)}, $nombreUsuarioOnline" else stringResource(id = R.string.usuario_hola),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            } else {
                Text(
                    text = stringResource(id = R.string.usuario_bienvenido),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(5.dp))
                // Etiqueta informativa de que no hay sesión online
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.usuario_sin_sesion_online),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Tarjeta de estadísticas: goles, asistencias, promedio y partidos
            if (sesionOnlineActiva) {
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .border(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(listOf(blue, violet)),
                            shape = RoundedCornerShape(17.dp)
                        )
                        .clip(RoundedCornerShape(17.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 10.dp)
                    ) {
                        // Fila con tres métricas principales
                        Row(
                            Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Columna: Goles
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(stringResource(id = R.string.usuario_goles_label), color = MaterialTheme.colorScheme.tertiary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    goles?.toString() ?: "-",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            // Columna: Asistencias
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(stringResource(id = R.string.usuario_asistencias_label), color = MaterialTheme.colorScheme.tertiary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    asistencias?.toString() ?: "-",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            // Columna: Promedio de goles
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(stringResource(id = R.string.usuario_promedio_label), color = MaterialTheme.colorScheme.tertiary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    if (promedioGoles != null) String.format("%.2f", promedioGoles) else "-",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        // Línea inferior con partidos jugados (si hay dato)
                        if (partidosJugados != null) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = stringResource(id = R.string.usuario_partidos_jugados_prefix) + partidosJugados.toString(),
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Botones de acceso si no hay sesión: Iniciar sesión / Crear cuenta
            if (!sesionOnlineActiva) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .widthIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(11.dp)
                ) {
                    // Botón: ir a login
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
                                    listOf(
                                        MaterialTheme.colorScheme.surface,
                                        MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            )
                            .clickable { navController.navigate("login") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(id = R.string.gen_iniciar_sesion),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    // Botón: ir a registro
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
                                    listOf(
                                        MaterialTheme.colorScheme.surface,
                                        MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            )
                            .clickable { navController.navigate("register") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(id = R.string.gen_crear_cuenta),
                            color = TorneoYaPalette.blue,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

        }
    }
}

@Composable
private fun CustomCerrarSesionDialog(
    onConfirm: () -> Unit,   // Acción al confirmar cierre
    onDismiss: () -> Unit,   // Acción al cancelar
    blue: Color,
    violet: Color,
    rojo: Color,
    accent: Color,
    background: Brush,
    lightText: Color,
    mutedText: Color
) {
    // Diálogo personalizado de confirmación de cierre de sesión
    Dialog(onDismissRequest = onDismiss) {
        // Contenedor con borde degradado
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .border(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(listOf(blue, violet)),
                    shape = RoundedCornerShape(18.dp)
                )
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                )
        ) {
            // Contenido del diálogo: título, texto y botones
            Column(
                Modifier
                    .padding(horizontal = 22.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.usuario_confirmar_cerrar_sesion_title),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(Modifier.height(11.dp))
                Text(
                    text = stringResource(id = R.string.usuario_confirmar_cerrar_sesion_message),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(25.dp))
                // Fila de botones: Sí / Cancelar
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Botón confirmar
                    OutlinedButton(
                        onClick = onConfirm,
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(listOf(rojo, violet))
                        ),
                        shape = RoundedCornerShape(11.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            stringResource(id = R.string.usuario_si_button),
                            color = rojo,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    // Botón cancelar
                    OutlinedButton(
                        onClick = onDismiss,
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(listOf(blue, violet))
                        ),
                        shape = RoundedCornerShape(11.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            stringResource(id = R.string.gen_cancelar),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
