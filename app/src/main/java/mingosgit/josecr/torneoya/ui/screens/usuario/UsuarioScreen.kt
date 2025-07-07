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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mingosgit.josecr.torneoya.viewmodel.usuario.UsuarioLocalViewModel
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import mingosgit.josecr.torneoya.viewmodel.usuario.GlobalUserViewModel
import mingosgit.josecr.torneoya.ui.screens.amigos.AmigosScreen

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
    val nombreActual = usuario?.nombre ?: "Usuario1"
    val nombreUsuarioOnline by globalUserViewModel.nombreUsuarioOnline.collectAsState()
    val sesionOnlineActiva by globalUserViewModel.sesionOnlineActiva.collectAsState(initial = false)

    var editando by remember { mutableStateOf(false) }
    var textFieldValue by remember { mutableStateOf(TextFieldValue(nombreActual)) }
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

    val puedeEditarImagen = editando

    LaunchedEffect(nombreActual) {
        if (!editando) {
            textFieldValue = TextFieldValue(
                text = nombreActual,
                selection = TextRange(nombreActual.length)
            )
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = { editando = true },
                enabled = !editando
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Editar nombre de usuario")
            }
            IconButton(onClick = { /* Futuro: navegación ajustes */ }) {
                Icon(Icons.Default.Settings, contentDescription = "Ajustes")
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDADADA))
                    .then(
                        if (puedeEditarImagen) Modifier.clickable { showDialog = true } else Modifier
                    ),
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
                        )
                    } else {
                        Text(text = "👤", fontSize = 48.sp)
                    }
                } else {
                    Text(text = "👤", fontSize = 48.sp)
                }
            }
            Spacer(modifier = Modifier.height(28.dp))

            if (!sesionOnlineActiva) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { navController.navigate("login") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp)
                    ) {
                        Text("Inicia sesión")
                    }
                    Button(
                        onClick = { navController.navigate("register") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    ) {
                        Text("Crear cuenta")
                    }
                }
            } else {
                Button(
                    onClick = {
                        showCerrarSesionDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp)
                ) {
                    Text("Cerrar sesión")
                }
            }
            var showAmigosScreen by remember { mutableStateOf(false) }
            if (!editando) {
                Text(
                    text = "Bienvenido/a $nombreActual / ${nombreUsuarioOnline ?: "---"}",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Button(
                    onClick = { navController.navigate("mis_jugadores") },
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    Text("Mis Jugadores")
                }

                Button(
                    onClick = { navController.navigate("partidos_lista_busqueda") },
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    Text("Administrar partidos")
                }

                Button(
                    onClick = { navController.navigate("equipos_predefinidos") },
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    Text("Equipos predefinidos")
                }
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = { navController.navigate("amigos") },
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Icon(Icons.Default.Group, contentDescription = "Amigos", modifier = Modifier.padding(end = 8.dp))
                    Text("Amigos")
                }

                // --- FIN BOTÓN AMIGOS ---
            }

            if (editando) {
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = {
                        textFieldValue = it
                    },
                    label = { Text("Tu nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.85f)
                )
                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            usuarioLocalViewModel.cambiarNombre(textFieldValue.text)
                            editando = false
                        }
                    ) {
                        Text("Guardar")
                    }
                    OutlinedButton(
                        onClick = {
                            textFieldValue = TextFieldValue(nombreActual)
                            editando = false
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }
    // State para mostrar u ocultar AmigosScreen como modal/dialog
    var showAmigosScreen by remember { mutableStateOf(false) }
}
