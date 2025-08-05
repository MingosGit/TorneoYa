package mingosgit.josecr.torneoya.ui.screens.usuario

import android.graphics.BitmapFactory
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
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
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
    LaunchedEffect(Unit) {
        usuarioLocalViewModel.cargarUsuario()
        globalUserViewModel.cargarNombreUsuarioOnlineSiSesionActiva()
    }

    val usuario by usuarioLocalViewModel.usuario.collectAsState()
    val nombreUsuarioOnline by globalUserViewModel.nombreUsuarioOnline.collectAsState()
    val sesionOnlineActiva by globalUserViewModel.sesionOnlineActiva.collectAsState(initial = false)
    val avatar by globalUserViewModel.avatar.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var showCerrarSesionDialog by remember { mutableStateOf(false) }
    var cropUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    // --- NUEVO: GOLES, ASISTENCIAS Y PROMEDIO ---
    var goles by remember { mutableStateOf<Int?>(null) }
    var asistencias by remember { mutableStateOf<Int?>(null) }
    var partidosJugados by remember { mutableStateOf<Int?>(null) }
    var promedioGoles by remember { mutableStateOf<Double?>(null) }
    val context = LocalContext.current

    LaunchedEffect(sesionOnlineActiva, nombreUsuarioOnline) {
        if (sesionOnlineActiva) {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val uid = user.uid
                val db = FirebaseFirestore.getInstance()
                // Goles
                val golesSnapshot = db.collection("goleadores")
                    .whereEqualTo("jugadorUid", uid)
                    .get().await()
                goles = golesSnapshot.size()

                // Asistencias
                val asistenciasSnapshot = db.collection("goleadores")
                    .whereEqualTo("asistenciaJugadorUid", uid)
                    .get().await()
                asistencias = asistenciasSnapshot.size()
                // Partidos jugados (al menos un gol o asistencia)
                val partidoGoles = golesSnapshot.documents.mapNotNull { it.getString("partidoUid") }
                val partidoAsistencias = asistenciasSnapshot.documents.mapNotNull { it.getString("partidoUid") }
                val partidosSet = (partidoGoles + partidoAsistencias).toSet()
                partidosJugados = partidosSet.size
                promedioGoles = if (partidosJugados ?: 0 > 0) {
                    (goles?.toDouble() ?: 0.0) / (partidosJugados?.toDouble() ?: 1.0)
                } else {
                    0.0
                }
            }
        } else {
            goles = null
            asistencias = null
            partidosJugados = null
            promedioGoles = null
        }
    }

    val selectImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            cropUri = uri
        }
    }

    val modernBackground = Brush.verticalGradient(
        0.0f to Color(0xFF1B1D29),
        0.28f to Color(0xFF212442),
        0.58f to Color(0xFF191A23),
        1.0f to Color(0xFF14151B)
    )
    val blue = TorneoYaPalette.blue
    val violet = TorneoYaPalette.violet
    val accent = TorneoYaPalette.accent
    val lightText = TorneoYaPalette.textLight
    val mutedText = TorneoYaPalette.mutedText
    val rojo = Color(0xFFFF2D55)

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Cambiar foto de perfil") },
            text = { Text("¿Quieres seleccionar una foto de tu galería?") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    selectImageLauncher.launch("image/*")
                }) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    if (showCerrarSesionDialog) {
        CustomCerrarSesionDialog(
            onConfirm = {
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
            background = Color(0xFF191A23),
            lightText = lightText,
            mutedText = mutedText
        )
    }

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

    Scaffold(
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(modernBackground)
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 6.dp, // TOP DINÁMICO Y PEGADO
                        start = 18.dp,
                        end = 18.dp,
                        bottom = 14.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    "Perfil",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 6.dp)
                )
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
                                    listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                                )
                            )
                            .clickable { showCerrarSesionDialog = true }
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cerrar sesión",
                            color = rojo,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(horizontal = 0.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
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
                                listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Ajustes",
                        tint = Color(0xFF8F5CFF),
                        modifier = Modifier.size(25.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Justo antes, recoge el avatar del GlobalUserViewModel
            val avatar by globalUserViewModel.avatar.collectAsState()

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
                            listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                        )
                    )
                    .clickable { navController.navigate("avatar") },
                contentAlignment = Alignment.Center
            ) {
                val context = LocalContext.current
                val avatarRes = if (avatar != null)
                    context.resources.getIdentifier("avatar_${avatar}", "drawable", context.packageName)
                else
                    context.resources.getIdentifier("avatar_placeholder", "drawable", context.packageName)

                if (avatarRes != 0) {
                    Image(
                        painter = painterResource(id = avatarRes),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Text("👤", fontSize = 52.sp)
                }
            }



            Spacer(modifier = Modifier.height(16.dp))

            if (sesionOnlineActiva) {
                Text(
                    text = if (!nombreUsuarioOnline.isNullOrBlank()) "Hola, $nombreUsuarioOnline" else "Hola",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                )
            } else {
                Text(
                    text = "Bienvenido",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                )
                Spacer(Modifier.height(5.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF22243A))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Sin sesión online",
                        fontSize = 13.sp,
                        color = mutedText,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ====== NUEVO: BLOQUE ESTADÍSTICAS DE GOLES Y ASISTENCIAS ======
            if (sesionOnlineActiva) {
                Spacer(modifier = Modifier.height(20.dp))
                // Sustituye el Card de estadísticas por este Box para tener el borde gradiente igual que los botones:
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .border(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(listOf(blue, violet)),
                            shape = RoundedCornerShape(17.dp)
                        )
                        .clip(RoundedCornerShape(17.dp))
                        .background(Color(0xFF23273D))
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 10.dp)
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Goles", color = accent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    goles?.toString() ?: "-",
                                    color = Color.White,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Asistencias", color = accent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    asistencias?.toString() ?: "-",
                                    color = Color.White,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Promedio", color = accent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    if (promedioGoles != null) String.format("%.2f", promedioGoles) else "-",
                                    color = Color.White,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        if (partidosJugados != null) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Partidos jugados: $partidosJugados",
                                    color = mutedText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                                )
                            }
                        }
                    }
                }

            }
            // ===============================================================

            Spacer(modifier = Modifier.height(26.dp))

            if (!sesionOnlineActiva) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .widthIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(11.dp)
                ) {
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
                }
            }

        }
    }
}
@Composable
private fun CustomCerrarSesionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    blue: Color,
    violet: Color,
    rojo: Color,
    accent: Color,
    background: Color,
    lightText: Color,
    mutedText: Color
) {
    Dialog(onDismissRequest = onDismiss) {
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
                .background(background)
        ) {
            Column(
                Modifier
                    .padding(horizontal = 22.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "¿Cerrar sesión?",
                    color = lightText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(Modifier.height(11.dp))
                Text(
                    text = "¿Estás seguro que quieres cerrar sesión?",
                    color = mutedText,
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(25.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
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
                            "SI",
                            color = rojo,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(14.dp))
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
                            "Cancelar",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}