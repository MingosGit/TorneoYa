package mingosgit.josecr.torneoya.ui.screens.usuario

import android.content.Context
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.Delete
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
import java.io.File

import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.viewmodel.usuario.GlobalUserViewModel

@Composable
fun UsuarioScreen(
    usuarioLocalViewModel: UsuarioLocalViewModel,
    navController: NavController,
    globalUserViewModel: GlobalUserViewModel
) {
    LaunchedEffect(Unit) {
        usuarioLocalViewModel.cargarUsuario()
    }

    val usuario by usuarioLocalViewModel.usuario.collectAsState()
    val nombreActual = usuario?.nombre ?: "Usuario1"
    val nombreUsuarioOnline by globalUserViewModel.nombreUsuarioOnline.collectAsState()

    var editando by remember { mutableStateOf(false) }
    var textFieldValue by remember { mutableStateOf(TextFieldValue(nombreActual)) }
    var showDialog by remember { mutableStateOf(false) }
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

    cropUri?.let { uriParaCropear ->
        CropImageDialog(
            uri = uriParaCropear,
            onDismiss = { cropUri = null },
            onCropDone = { croppedPath ->
                usuarioLocalViewModel.cambiarFotoPerfil(croppedPath)
                cropUri = null
            }
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
}
