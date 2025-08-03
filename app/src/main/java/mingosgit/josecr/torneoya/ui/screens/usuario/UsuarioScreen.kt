package mingosgit.josecr.torneoya.ui.screens.usuario

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.NavHostController
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

    var showDialog by remember { mutableStateOf(false) }
    var showCerrarSesionDialog by remember { mutableStateOf(false) }
    var cropUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

    val selectImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            cropUri = uri
        }
    }

    val blue = TorneoYaPalette.blue
    val violet = TorneoYaPalette.violet
    val accent = TorneoYaPalette.accent
    val lightText = TorneoYaPalette.textLight
    val mutedText = TorneoYaPalette.mutedText

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
            },
            onDismiss = { showCerrarSesionDialog = false },
            blue = blue,
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

    // SIN topBar en Scaffold, header va dentro del Column
    Scaffold(
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // HEADER IDENTICO AL DE AMIGOS
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Perfil",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 18.dp) // SOLO padding interno, igual que el texto de amigos
                    )
                    IconButton(
                        onClick = { navController.navigate("ajustes") },
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ajustes",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }



            }

            Spacer(modifier = Modifier.height(18.dp))

            // AVATAR SIMPLE REDONDO, FONDO LISO
            Box(
                modifier = Modifier
                    .size(118.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF22243A))
                    .clickable { showDialog = true },
                contentAlignment = Alignment.Center
            ) {
                val fotoPerfilPath = usuario?.fotoPerfilPath
                if (!fotoPerfilPath.isNullOrEmpty()) {
                    val bitmap = BitmapFactory.decodeFile(fotoPerfilPath)
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22243A))
                        )
                    } else {
                        Text("👤", fontSize = 52.sp)
                    }
                } else {
                    Text("👤", fontSize = 52.sp)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // USUARIO INFO LIMPIO
            if (sesionOnlineActiva) {
                Text(
                    text = if (!nombreUsuarioOnline.isNullOrBlank()) "Hola, $nombreUsuarioOnline" else "Hola",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = lightText,
                )
                Spacer(Modifier.height(5.dp))
                // Badge azul institucional
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(TorneoYaPalette.blue)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Sesión online activa",
                        fontSize = 13.sp,
                        color = TorneoYaPalette.textLight,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                    )
                }
            } else {
                Text(
                    text = "Bienvenido",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = lightText,
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

            Spacer(modifier = Modifier.height(26.dp))

            // BOTÓN ACCIÓN PRINCIPAL, SIN SOMBRAS
            if (!sesionOnlineActiva) {
                Button(
                    onClick = { navController.navigate("login") },
                    modifier = Modifier
                        .widthIn(max = 400.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = blue),
                    shape = RoundedCornerShape(13.dp)
                ) {
                    Text("Iniciar sesión o Crear cuenta", color = lightText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            } else {
                OutlinedButton(
                    onClick = { showCerrarSesionDialog = true },
                    modifier = Modifier
                        .widthIn(max = 400.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = accent
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(listOf(accent, violet))
                    ),
                    shape = RoundedCornerShape(13.dp)
                ) {
                    Text("Cerrar sesión", color = accent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // OPCIÓN AMIGOS SÓLIDA, SIMPLE
            PerfilOpcionAmigos(
                modifier = Modifier.widthIn(max = 400.dp) // Limita el ancho
            ) {
                navController.navigate("amigos")
            }
        }
    }
}

@Composable
private fun PerfilOpcionAmigos(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val blue = TorneoYaPalette.blue
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(Color(0xFF23253A))
            .clickable { onClick() }
            .padding(horizontal = 19.dp, vertical = 17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Group,
            contentDescription = "Amigos",
            modifier = Modifier.size(25.dp),
            tint = blue
        )
        Spacer(Modifier.width(15.dp))
        Column {
            Text(
                text = "Amigos",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = "Gestiona tus amigos y añade nuevos contactos",
                fontSize = 13.sp,
                color = TorneoYaPalette.mutedText
            )
        }
    }
}


@Composable
private fun CustomCerrarSesionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    blue: Color,
    accent: Color,
    background: Color,
    lightText: Color,
    mutedText: Color
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = background,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
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
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = accent),
                        shape = RoundedCornerShape(11.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Sí, cerrar sesión", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(14.dp))
                    OutlinedButton(
                        onClick = onDismiss,
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(blue, Color.White.copy(alpha = 0.22f)))),
                        shape = RoundedCornerShape(11.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar", color = blue, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
