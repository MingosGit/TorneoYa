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
import androidx.compose.ui.draw.shadow
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

    val modernBackground = Brush.verticalGradient(
        0.0f to Color(0xFF181B26),
        0.25f to Color(0xFF22263B),
        0.6f to Color(0xFF1A1E29),
        1.0f to Color(0xFF161622)
    )
    val blue = Color(0xFF296DFF)
    val violet = Color(0xFF8F5CFF)
    val accent = Color(0xFFFFB531)
    val lightText = Color(0xFFF7F7FF)
    val mutedText = Color(0xFFB7B7D1)
    val chipBg = Color(0xFF24294A)

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
            background = Color(0xFF232A3A),
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
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text("Perfil", fontSize = 20.sp, color = lightText, fontWeight = FontWeight.Bold)
                },
                actions = {
                    IconButton(onClick = { navController.navigate("ajustes") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Ajustes", tint = blue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(modernBackground)
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(132.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(violet.copy(alpha = 0.20f), blue.copy(alpha = 0.24f), Color.Transparent),
                            radius = 220f
                        )
                    )
                    .clickable { showDialog = true }
                    .shadow(12.dp, CircleShape),
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
                                .size(128.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2A2B3B))
                        )
                    } else {
                        Text("👤", fontSize = 56.sp)
                    }
                } else {
                    Text("👤", fontSize = 56.sp)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (sesionOnlineActiva) {
                Text(
                    text = if (!nombreUsuarioOnline.isNullOrBlank()) "Hola, $nombreUsuarioOnline" else "Hola",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = lightText,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Sesión online activa",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = accent.copy(alpha = 0.9f)
                    )
                )
            } else {
                Text(
                    text = "Bienvenido",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = lightText,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Sin sesión online",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = mutedText
                    )
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            if (!sesionOnlineActiva) {
                Button(
                    onClick = { navController.navigate("login") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = blue),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Text("Iniciar sesión o Crear cuenta", color = lightText, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = { showCerrarSesionDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF292E3E)),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Text("Cerrar sesión", color = accent, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(5.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF181B2F)),
                tonalElevation = 0.dp,
                color = Color(0xFF181B2F)
            ) {
                Column(
                    Modifier
                        .padding(horizontal = 0.dp, vertical = 2.dp)
                ) {
                    PerfilOpcionAmigos {
                        navController.navigate("amigos")
                    }
                }
            }
        }
    }
}

@Composable
private fun PerfilOpcionAmigos(
    onClick: () -> Unit
) {
    val blue = Color(0xFF296DFF)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 22.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Group,
            contentDescription = "Amigos",
            modifier = Modifier.size(27.dp),
            tint = blue
        )
        Spacer(Modifier.width(18.dp))
        Column {
            Text(
                text = "Amigos",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = "Gestiona tus amigos y añade nuevos contactos",
                fontSize = 14.sp,
                color = Color(0xFFB7B7D1)
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
            shape = RoundedCornerShape(19.dp),
            color = background,
            tonalElevation = 0.dp,
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
        ) {
            Column(
                Modifier
                    .padding(horizontal = 26.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "¿Cerrar sesión?",
                    color = lightText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "¿Estás seguro que quieres cerrar sesión?",
                    color = mutedText,
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(28.dp))
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
                    Spacer(Modifier.width(16.dp))
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
