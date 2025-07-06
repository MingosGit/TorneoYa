package mingosgit.josecr.torneoya.ui.screens.amigos

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.viewmodel.amigos.AmigosViewModel
import mingosgit.josecr.torneoya.viewmodel.amigos.AgregarAmigoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmigosScreen(
    navController: NavController,
    amigosViewModel: AmigosViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = AmigosViewModel.Factory()
    ),
    agregarAmigoViewModel: AgregarAmigoViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = AgregarAmigoViewModel.Factory()
    )
) {
    val amigos by amigosViewModel.amigos.collectAsState()
    val solicitudes by amigosViewModel.solicitudes.collectAsState()
    val mensaje by amigosViewModel.mensaje.collectAsState()

    val agregarUiState by agregarAmigoViewModel.uiState.collectAsState()
    val userUid by agregarAmigoViewModel.miUid.collectAsState()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            // TopBar personalizada con icono de solicitudes pendientes SIEMPRE visible
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Amigos",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )

                // SIEMPRE visible
                if (solicitudes.isNotEmpty()) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = Color.White
                            ) {
                                Text("${solicitudes.size}")
                            }
                        }
                    ) {
                        IconButton(onClick = { navController.navigate("solicitudes_pendientes") }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Solicitudes de amistad"
                            )
                        }
                    }
                } else {
                    // Sin badge si no hay solicitudes
                    IconButton(onClick = { navController.navigate("solicitudes_pendientes") }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Solicitudes de amistad"
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
            Text("Tus amigos", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(6.dp))
            if (amigos.isEmpty()) {
                Text(
                    "No tienes amigos todavía",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else {
                LazyColumn(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                    items(amigos) { amigo ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                amigo.nombreUsuario,
                                Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            IconButton(onClick = { amigosViewModel.eliminarAmigo(amigo.uid) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar amigo")
                            }
                        }
                        Divider()
                    }
                }
            }
        }

        // FAB Botón agregar amigo
        FloatingActionButton(
            onClick = { showSheet = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Agregar amigo")
        }

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false; agregarAmigoViewModel.resetUi() },
                sheetState = bottomSheetState,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                dragHandle = {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            Modifier
                                .width(36.dp)
                                .height(6.dp)
                                .background(Color.LightGray, RoundedCornerShape(3.dp))
                        )
                    }
                }
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Tu UID", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
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
                    Spacer(Modifier.height(22.dp))
                    Text("Buscar por UID", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    var amigoUidInput by remember { mutableStateOf("") }

                    OutlinedTextField(
                        value = amigoUidInput,
                        onValueChange = { amigoUidInput = it },
                        label = { Text("UID del amigo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = {
                                val clip = clipboard.getText()
                                if (clip != null) amigoUidInput = clip.text
                            }) {
                                Icon(Icons.Default.ContentPaste, contentDescription = "Pegar UID")
                            }
                        }
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            agregarAmigoViewModel.buscarPorUid(amigoUidInput)
                        },
                        enabled = amigoUidInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Buscar")
                    }

                    when (agregarUiState) {
                        is AgregarAmigoViewModel.UiState.Busqueda -> {
                            val usuario = (agregarUiState as AgregarAmigoViewModel.UiState.Busqueda).usuario
                            Spacer(Modifier.height(24.dp))
                            Text("¿Este es tu amigo?", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Card(
                                Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("Nombre: ${usuario.nombreUsuario}", style = MaterialTheme.typography.bodyLarge)
                                    Text("UID: ${usuario.uid}", style = MaterialTheme.typography.bodyMedium)
                                    Spacer(Modifier.height(10.dp))
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        Button(onClick = {
                                            agregarAmigoViewModel.enviarSolicitud(usuario.uid)
                                            amigoUidInput = ""
                                        }) {
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

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}
