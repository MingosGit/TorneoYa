package mingosgit.josecr.torneoya.ui.screens.amigos

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.viewmodel.amigos.AmigosViewModel
import mingosgit.josecr.torneoya.viewmodel.amigos.AgregarAmigoViewModel
import mingosgit.josecr.torneoya.viewmodel.usuario.GlobalUserViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AmigosScreen(
    navController: NavController,
    globalUserViewModel: GlobalUserViewModel,
    amigosViewModel: AmigosViewModel = viewModel(
        factory = AmigosViewModel.Factory()
    ),
    agregarAmigoViewModel: AgregarAmigoViewModel = viewModel(
        factory = AgregarAmigoViewModel.Factory()
    ),
    modifier: Modifier = Modifier,
) {
    val sesionOnlineActiva by globalUserViewModel.sesionOnlineActiva.collectAsState()
    LaunchedEffect(Unit) { globalUserViewModel.cargarNombreUsuarioOnlineSiSesionActiva() }
    LaunchedEffect(Unit) { amigosViewModel.cargarAmigosYSolicitudes() }

    val modernBackground = Brush.verticalGradient(
        0.0f to Color(0xFF1B1D29),
        0.28f to Color(0xFF212442),
        0.58f to Color(0xFF191A23),
        1.0f to Color(0xFF14151B)
    )

    if (!sesionOnlineActiva) {
        Box(
            Modifier
                .fillMaxSize()
                .background(modernBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF296DFF).copy(alpha = 0.13f),
                    shadowElevation = 0.dp,
                    modifier = Modifier.size(85.dp)
                ) {
                    Icon(
                        Icons.Default.PersonAdd,
                        contentDescription = "Icono amigos",
                        tint = Color(0xFF296DFF),
                        modifier = Modifier.padding(22.dp)
                    )
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "Acceso a Amigos",
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    text = "Inicia sesión para acceder a tu lista de amigos, enviar solicitudes y gestionar contactos.",
                    fontSize = 16.sp,
                    color = Color(0xFFB7B7D1),
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontWeight = FontWeight.Normal,
                )
                Spacer(Modifier.height(32.dp))

                // Botón Iniciar sesión con borde gradiente y fondo acorde
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
                Spacer(Modifier.height(11.dp))

                // Botón Crear cuenta con borde gradiente y fondo acorde
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

                Spacer(Modifier.height(26.dp))

                Text(
                    text = "¿Prefieres usar una cuenta local?\nPuedes configurarla en Ajustes de Usuario.",
                    fontSize = 14.sp,
                    color = Color(0xFFB7B7D1),
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.fillMaxWidth()
                )
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
    var expandedUid by remember { mutableStateOf<String?>(null) }
    var showEliminarDialog by remember { mutableStateOf(false) }
    var uidAEliminar by remember { mutableStateOf<String?>(null) }

    Box(
        Modifier
            .fillMaxSize()
            .background(modernBackground)
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Amigos",
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(
                                listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                            ),
                            shape = CircleShape
                        )
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                            )
                        )
                        .clickable { navController.navigate("solicitudes_pendientes") },
                    contentAlignment = Alignment.Center
                ) {
                    if (solicitudes.isNotEmpty()) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = TorneoYaPalette.violet,
                                    contentColor = Color.White,
                                ) {
                                    Text("${solicitudes.size}")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Solicitudes de amistad",
                                tint = Color(0xFF8F5CFF),
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Solicitudes de amistad",
                            tint = Color(0xFF8F5CFF),
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            if (amigos.isEmpty()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No tienes amigos todavía",
                        color = Color(0xFFB7B7D1),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                }
            } else {
                LazyColumn(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 10.dp)
                ) {
                    items(amigos) { amigo ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp, horizontal = 6.dp)
                                .clip(RoundedCornerShape(17.dp))
                                .border(
                                    width = 2.dp,
                                    brush = Brush.horizontalGradient(
                                        listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                                    ),
                                    shape = RoundedCornerShape(17.dp)
                                )
                                .background(Color(0xFF23273D))
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = { expandedUid = amigo.uid }
                                ),
                            color = Color.Transparent,
                            shadowElevation = 0.dp
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFF296DFF).copy(alpha = 0.22f), Color(0xFF8F5CFF).copy(alpha = 0.17f))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        amigo.nombreUsuario.take(1).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 21.sp
                                    )
                                }
                                Spacer(Modifier.width(15.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        amigo.nombreUsuario,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 17.sp,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        amigo.uid,
                                        fontSize = 13.sp,
                                        color = Color(0xFFB7B7D1),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Box {
                                    IconButton(
                                        onClick = { expandedUid = amigo.uid }
                                    ) {
                                        Icon(
                                            Icons.Default.MoreHoriz,
                                            contentDescription = "Opciones",
                                            tint = Color(0xFF8F5CFF)
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = expandedUid == amigo.uid,
                                        onDismissRequest = { expandedUid = null }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Copiar código de amigo") },
                                            onClick = {
                                                clipboard.setText(androidx.compose.ui.text.AnnotatedString(amigo.uid))
                                                Toast.makeText(context, "Copiado", Toast.LENGTH_SHORT).show()
                                                expandedUid = null
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Eliminar amigo", color = Color(0xFFFF7675)) },
                                            onClick = {
                                                uidAEliminar = amigo.uid
                                                showEliminarDialog = true
                                                expandedUid = null
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF7675))
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = { showSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .height(50.dp)
                .clip(RoundedCornerShape(15.dp)),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color(0xFF296DFF)
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                )
            )
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = "Añadir amigo", modifier = Modifier.size(24.dp), tint = Color(0xFF296DFF))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Añadir amigo", color = Color(0xFF296DFF), fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = 2.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .background(Color(0xFF23273D))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(userUid ?: "Cargando...", color = Color.White, fontSize = 15.sp)
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = {
                                userUid?.let {
                                    clipboard.setText(androidx.compose.ui.text.AnnotatedString(it))
                                    Toast.makeText(context, "Copiado", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar UID", tint = Color(0xFF8F5CFF))
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = 2.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .background(Color(0xFF23273D))
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            "Buscar por UID",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    Spacer(Modifier.height(10.dp))
                    var amigoUidInput by remember { mutableStateOf("") }

                    OutlinedTextField(
                        value = amigoUidInput,
                        onValueChange = { amigoUidInput = it },
                        label = { Text("UID del amigo", color = Color(0xFF8F5CFF)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp)),
                        shape = RoundedCornerShape(10.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            containerColor = Color(0xFF191A23),
                            focusedBorderColor = Color(0xFF8F5CFF),
                            unfocusedBorderColor = Color(0xFF23273D)
                        ),
                        trailingIcon = {
                            IconButton(onClick = {
                                val clip = clipboard.getText()
                                if (clip != null) amigoUidInput = clip.text
                            }) {
                                Icon(Icons.Default.ContentPaste, contentDescription = "Pegar UID", tint = Color(0xFF8F5CFF))
                            }
                        }
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = {
                            agregarAmigoViewModel.buscarPorUid(amigoUidInput)
                        },
                        enabled = amigoUidInput.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(
                                listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                            )
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF23273D),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Buscar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }


                    when (agregarUiState) {
                        is AgregarAmigoViewModel.UiState.Busqueda -> {
                            val usuario = (agregarUiState as AgregarAmigoViewModel.UiState.Busqueda).usuario
                            Spacer(Modifier.height(22.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(13.dp))
                                    .border(
                                        width = 2.dp,
                                        brush = Brush.horizontalGradient(
                                            listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                                        ),
                                        shape = RoundedCornerShape(13.dp)
                                    )
                                    .background(Color(0xFF23273D))
                                    .padding(18.dp)
                            ) {
                                Column {
                                    Text("Nombre: ${usuario.nombreUsuario}", color = Color(0xFFB7B7D1))
                                    Text("UID: ${usuario.uid}", color = Color(0xFFB7B7D1))
                                    Spacer(Modifier.height(10.dp))
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                agregarAmigoViewModel.enviarSolicitud(usuario.uid)
                                                amigoUidInput = ""
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                                width = 2.dp,
                                                brush = Brush.horizontalGradient(
                                                    listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                                                )
                                            ),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = Color(0xFF23273D),
                                                contentColor = Color.White
                                            ),
                                            modifier = Modifier.height(44.dp)
                                        ) {
                                            Text("Sí, enviar solicitud", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        }

                                    }
                                }
                            }
                        }
                        is AgregarAmigoViewModel.UiState.Error -> {
                            Spacer(Modifier.height(18.dp))
                            Text(
                                (agregarUiState as AgregarAmigoViewModel.UiState.Error).mensaje,
                                color = Color.Red
                            )
                        }
                        is AgregarAmigoViewModel.UiState.Exito -> {
                            Spacer(Modifier.height(18.dp))
                            Text(
                                "¡Solicitud enviada!",
                                color = Color(0xFF2ecc71)
                            )
                        }
                        else -> {}
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }

        if (showEliminarDialog && uidAEliminar != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .border(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(
                                listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                            ),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .background(Color(0xFF191A23))
                        .padding(26.dp)
                        .widthIn(min = 270.dp, max = 340.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Eliminar amigo", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 19.sp)
                        Spacer(Modifier.height(9.dp))
                        Text(
                            "¿Estás seguro que deseas eliminar a este amigo? Esta acción no se puede deshacer.",
                            color = Color(0xFFB7B7D1),
                            fontSize = 15.sp
                        )
                        Spacer(Modifier.height(22.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = {
                                    showEliminarDialog = false
                                    uidAEliminar = null
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    width = 2.dp,
                                    brush = Brush.horizontalGradient(
                                        listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                                    )
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                            ) {
                                Text(
                                    "Cancelar",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            OutlinedButton(
                                onClick = {
                                    amigosViewModel.eliminarAmigo(uidAEliminar!!)
                                    showEliminarDialog = false
                                    uidAEliminar = null
                                },
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    width = 2.dp,
                                    brush = Brush.horizontalGradient(
                                        listOf(Color(0xFFFF7675), TorneoYaPalette.violet)
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = Color(0xFFFF7675)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                            ) {
                                Text(
                                    "Eliminar",
                                    color = Color(0xFFFF7675),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
