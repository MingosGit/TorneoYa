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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.saveable.rememberSaveable
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
        AlertDialog(
            onDismissRequest = { showCerrarSesionDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro que quieres cerrar sesión?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        globalUserViewModel.cerrarSesionOnline()
                        showCerrarSesionDialog = false
                    }
                ) {
                    Text("Sí, cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCerrarSesionDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
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
        topBar = {
            TopAppBar(
                title = { Text("Perfil", fontSize = 20.sp) },
                actions = {
                    IconButton(onClick = { navController.navigate("ajustes") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Ajustes")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0))
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
                                .size(120.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Text("👤", fontSize = 50.sp)
                    }
                } else {
                    Text("👤", fontSize = 50.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (sesionOnlineActiva) {
                Text(
                    text = "Hola, $nombreUsuarioOnline",
                    style = MaterialTheme.typography.headlineSmall
                )
            } else {
                Text(
                    text = "Bienvenido",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!sesionOnlineActiva) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { navController.navigate("login") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Iniciar sesión")
                    }
                    Button(
                        onClick = { navController.navigate("register") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Crear cuenta")
                    }
                }
            } else {
                Button(
                    onClick = { showCerrarSesionDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text("Cerrar sesión")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Divider()

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PerfilOpcionItem("Mis Jugadores") { navController.navigate("mis_jugadores") }
                PerfilOpcionItem("Administrar Partidos") { navController.navigate("partidos_lista_busqueda") }
                PerfilOpcionItem("Equipos Predefinidos") { navController.navigate("equipos_predefinidos") }
                PerfilOpcionItem("Amigos", showIcon = true) { navController.navigate("amigos") }
            }
        }
    }
}

@Composable
private fun PerfilOpcionItem(
    texto: String,
    showIcon: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = texto,
                modifier = Modifier.weight(1f),
                fontSize = 16.sp
            )
            if (showIcon) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "Ir",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
