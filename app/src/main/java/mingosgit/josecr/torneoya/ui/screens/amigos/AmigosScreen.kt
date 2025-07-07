package mingosgit.josecr.torneoya.ui.screens.amigos

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.viewmodel.amigos.AmigosViewModel
import mingosgit.josecr.torneoya.viewmodel.amigos.AgregarAmigoViewModel
import mingosgit.josecr.torneoya.viewmodel.usuario.GlobalUserViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmigosScreen(
    navController: NavController,
    globalUserViewModel: GlobalUserViewModel,
    amigosViewModel: AmigosViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = AmigosViewModel.Factory()
    ),
    agregarAmigoViewModel: AgregarAmigoViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = AgregarAmigoViewModel.Factory()
    ),
) {
    val sesionOnlineActiva by globalUserViewModel.sesionOnlineActiva.collectAsState()
    LaunchedEffect(Unit) { globalUserViewModel.cargarNombreUsuarioOnlineSiSesionActiva() }
    LaunchedEffect(Unit) { amigosViewModel.cargarAmigosYSolicitudes() }

    if (!sesionOnlineActiva) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.surface)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .padding(32.dp)
                    .shadow(12.dp, RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        "Necesitas iniciar sesión para acceder a tus amigos",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                    Button(
                        onClick = { navController.navigate("login") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Iniciar sesión")
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { navController.navigate("register") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Crear cuenta")
                    }
                }
            }
        }
        return
    }

    val amigos by amigosViewModel.amigos.collectAsState()
    val solicitudes by amigosViewModel.solicitudes.collectAsState()
    val mensaje by amigosViewModel.mensaje.collectAsState()
    val agregarUiState by agregarAmigoViewModel.uiState.collectAsState()
    val userUid by agregarAmigoViewModel.miUid.collectAsState()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(Modifier.fillMaxSize()) {
            // TopBar visual mejorada
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Amigos",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                // Badge de solicitudes
                if (solicitudes.isNotEmpty()) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = Color.White,
                                modifier = Modifier.shadow(4.dp, CircleShape)
                            ) {
                                Text("${solicitudes.size}")
                            }
                        }
                    ) {
                        IconButton(onClick = { navController.navigate("solicitudes_pendientes") }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Solicitudes de amistad",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    IconButton(onClick = { navController.navigate("solicitudes_pendientes") }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Solicitudes de amistad",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(vertical = 16.dp, horizontal = 18.dp)) {
                    Text(
                        "Tus amigos",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    if (amigos.isEmpty()) {
                        Text(
                            "No tienes amigos todavía",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    } else {
                        LazyColumn(
                            Modifier
                                .fillMaxWidth()
                                .heightIn(max = 340.dp)
                        ) {
                            items(amigos) { amigo ->
                                Card(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surface)
                                            .padding(vertical = 14.dp, horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(
                                                    brush = Brush.linearGradient(
                                                        colors = listOf(
                                                            MaterialTheme.colorScheme.primaryContainer,
                                                            MaterialTheme.colorScheme.secondaryContainer
                                                        )
                                                    ),
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                amigo.nombreUsuario.take(1).uppercase(),
                                                color = MaterialTheme.colorScheme.primary,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        }
                                        Spacer(Modifier.width(14.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                amigo.nombreUsuario,
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                amigo.uid,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.outline,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        IconButton(
                                            onClick = { amigosViewModel.eliminarAmigo(amigo.uid) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Eliminar amigo",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { showSheet = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .shadow(10.dp, CircleShape)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Agregar amigo")
        }

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showSheet = false
                    agregarAmigoViewModel.resetUi()
                },
                sheetState = bottomSheetState,
                shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
                dragHandle = {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            Modifier
                                .width(42.dp)
                                .height(8.dp)
                                .background(Color.LightGray, RoundedCornerShape(5.dp))
                        )
                    }
                }
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Tu UID", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(userUid ?: "Cargando...", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = {
                            userUid?.let {
                                clipboard.setText(androidx.compose.ui.text.AnnotatedString(it))
                                Toast.makeText(context, "Copiado", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copiar UID")
                        }
                    }
                    Spacer(Modifier.height(26.dp))
                    Text("Buscar por UID", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))
                    var amigoUidInput by remember { mutableStateOf("") }

                    OutlinedTextField(
                        value = amigoUidInput,
                        onValueChange = { amigoUidInput = it },
                        label = { Text("UID del amigo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        trailingIcon = {
                            IconButton(onClick = {
                                val clip = clipboard.getText()
                                if (clip != null) amigoUidInput = clip.text
                            }) {
                                Icon(Icons.Default.ContentPaste, contentDescription = "Pegar UID")
                            }
                        }
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            agregarAmigoViewModel.buscarPorUid(amigoUidInput)
                        },
                        enabled = amigoUidInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Buscar")
                    }

                    when (agregarUiState) {
                        is AgregarAmigoViewModel.UiState.Busqueda -> {
                            val usuario = (agregarUiState as AgregarAmigoViewModel.UiState.Busqueda).usuario
                            Spacer(Modifier.height(22.dp))
                            Text("¿Este es tu amigo?", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Card(
                                Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(Modifier.padding(18.dp)) {
                                    Text("Nombre: ${usuario.nombreUsuario}", style = MaterialTheme.typography.bodyLarge)
                                    Text("UID: ${usuario.uid}", style = MaterialTheme.typography.bodyMedium)
                                    Spacer(Modifier.height(10.dp))
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Button(
                                            onClick = {
                                                agregarAmigoViewModel.enviarSolicitud(usuario.uid)
                                                amigoUidInput = ""
                                            },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Sí, enviar solicitud")
                                        }
                                    }
                                }
                            }
                        }
                        is AgregarAmigoViewModel.UiState.Error -> {
                            Spacer(Modifier.height(18.dp))
                             Text(
                                (agregarUiState as AgregarAmigoViewModel.UiState.Error).mensaje,
                                color = Color.Red,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        is AgregarAmigoViewModel.UiState.Exito -> {
                            Spacer(Modifier.height(18.dp))
                            Text(
                                "¡Solicitud enviada!",
                                color = Color(0xFF2ecc71),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        else -> {}
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}
