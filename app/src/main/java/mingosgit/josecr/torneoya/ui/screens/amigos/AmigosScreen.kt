// Archivo: AmigosScreen.kt

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
import androidx.compose.ui.res.painterResource
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
import androidx.compose.ui.res.stringResource
import mingosgit.josecr.torneoya.R

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
                        contentDescription = stringResource(id = R.string.gen_icono_amigos_desc),
                        tint = Color(0xFF296DFF),
                        modifier = Modifier.padding(22.dp)
                    )
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    text = stringResource(id = R.string.amigossc_acceso_titulo),
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    text = stringResource(id = R.string.amigossc_acceso_desc),
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
                        stringResource(id = R.string.gen_iniciar_sesion),
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
                        stringResource(id = R.string.gen_crear_cuenta),
                        color = TorneoYaPalette.blue,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }

                Spacer(Modifier.height(26.dp))

                Text(
                    text = stringResource(id = R.string.amigossc_cuenta_local),
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
                    text = stringResource(id = R.string.amigossc_titulo),
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
                                contentDescription = stringResource(id = R.string.amigossc_solicitudes_desc),
                                tint = Color(0xFF8F5CFF),
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = stringResource(id = R.string.amigossc_solicitudes_desc),
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
                        stringResource(id = R.string.amigossc_no_amigos),
                        color = Color(0xFFB7B7D1),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                }
            } else {
                var showToast by remember { mutableStateOf(false) }

                if (showToast) {
                    LaunchedEffect(Unit) {
                        Toast.makeText(context, context.getString(R.string.gen_copiado), Toast.LENGTH_SHORT).show()
                        showToast = false
                    }
                }
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
                                .clickable {
                                    navController.navigate("perfil_amigo/${amigo.uid}")
                                },
                            color = Color.Transparent,
                            shadowElevation = 0.dp
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // CORREGIDO: Mueve la obtención del avatarResId FUERA del contexto composable
                                val avatarNum = amigo.avatar ?: 0
                                val avatarResId = if (avatarNum > 0)
                                    LocalContext.current.resources.getIdentifier("avatar_$avatarNum", "drawable", LocalContext.current.packageName)
                                else
                                    LocalContext.current.resources.getIdentifier("avatar_placeholder", "drawable", LocalContext.current.packageName)

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
                                    if (avatarResId != 0) {
                                        Icon(
                                            painter = painterResource(id = avatarResId),
                                            contentDescription = stringResource(id = R.string.gen_avatar_desc),
                                            tint = Color.Unspecified,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                        )
                                    } else {
                                        Text(
                                            amigo.nombreUsuario.take(1).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 21.sp
                                        )
                                    }
                                }
                                Spacer(Modifier.width(15.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(id = R.string.amigossc_nombre_label, amigo.nombreUsuario),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 17.sp,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = stringResource(id = R.string.amigossc_uid_label, amigo.uid),
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
                                            contentDescription = stringResource(id = R.string.amigossc_opciones),
                                            tint = Color(0xFF8F5CFF)
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = expandedUid == amigo.uid,
                                        onDismissRequest = { expandedUid = null }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(id = R.string.amigossc_copiar_codigo)) },
                                            onClick = {
                                                clipboard.setText(androidx.compose.ui.text.AnnotatedString(amigo.uid))
                                                showToast = true
                                                expandedUid = null
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(stringResource(id = R.string.gen_eliminar_amigo), color = Color(0xFFFF7675)) },
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
            Icon(Icons.Default.PersonAdd, contentDescription = stringResource(id = R.string.amigossc_añadir_amigo), modifier = Modifier.size(24.dp), tint = Color(0xFF296DFF))
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(id = R.string.amigossc_añadir_amigo), color = Color(0xFF296DFF), fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                BottomSheetContent(
                    userUid = userUid,
                    clipboard = clipboard,
                    context = context,
                    agregarUiState = agregarUiState,
                    agregarAmigoViewModel = agregarAmigoViewModel
                )
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
                        Text(stringResource(id = R.string.gen_eliminar_titulo), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 19.sp)
                        Spacer(Modifier.height(9.dp))
                        Text(
                            stringResource(id = R.string.amigossc_eliminar_confirm),
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
                                    stringResource(id = R.string.gen_cancelar),
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
                                    stringResource(id = R.string.gen_eliminar),
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetContent(
    userUid: String?,
    clipboard: androidx.compose.ui.platform.ClipboardManager,
    context: android.content.Context,
    agregarUiState: AgregarAmigoViewModel.UiState,
    agregarAmigoViewModel: AgregarAmigoViewModel
) {
    var amigoUidInput by remember { mutableStateOf("") }
    var showToast by remember { mutableStateOf(false) }

    if (showToast) {
        LaunchedEffect(Unit) {
            android.widget.Toast.makeText(context, context.getString(R.string.gen_copiado), android.widget.Toast.LENGTH_SHORT).show()
            showToast = false
        }
    }

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
                Text(userUid ?: stringResource(id = R.string.gen_cargando), color = Color.White, fontSize = 15.sp)
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = {
                    userUid?.let {
                        clipboard.setText(androidx.compose.ui.text.AnnotatedString(it))
                        showToast = true
                    }
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = stringResource(id = R.string.gen_copiar_uid), tint = Color(0xFF8F5CFF))
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
                stringResource(id = R.string.amigossc_buscar_uid),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = amigoUidInput,
            onValueChange = { amigoUidInput = it },
            label = { Text(stringResource(id = R.string.amigossc_uid_amigo), color = Color(0xFF8F5CFF)) },
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
                    Icon(Icons.Default.ContentPaste, contentDescription = stringResource(id = R.string.gen_pegar_uid), tint = Color(0xFF8F5CFF))
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
            Text(stringResource(id = R.string.gen_buscar), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                        Text(stringResource(id = R.string.amigossc_nombre_label, usuario.nombreUsuario), color = Color(0xFFB7B7D1))
                        Text(stringResource(id = R.string.amigossc_uid_label, usuario.uid), color = Color(0xFFB7B7D1))
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
                                Text(stringResource(id = R.string.amigossc_si_enviar_solicitud), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }

                        }
                    }
                }
            }
            is AgregarAmigoViewModel.UiState.Error -> {
                Spacer(Modifier.height(18.dp))
                Text(
                    text = stringResource(id = (agregarUiState as AgregarAmigoViewModel.UiState.Error).resId),
                    color = Color.Red
                )
            }
            is AgregarAmigoViewModel.UiState.Exito -> {
                Spacer(Modifier.height(18.dp))
                Text(
                    stringResource(id = R.string.amigossc_solicitud_enviada),
                    color = Color(0xFF2ecc71)
                )
            }
            else -> {}
        }
        Spacer(Modifier.height(12.dp))
    }
}


