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
import androidx.compose.material.icons.materialIcon
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.R
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.ui.theme.mutedText
import mingosgit.josecr.torneoya.viewmodel.amigos.AgregarAmigoViewModel
import mingosgit.josecr.torneoya.viewmodel.amigos.AmigosViewModel
import mingosgit.josecr.torneoya.viewmodel.usuario.GlobalUserViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AmigosScreen(
    navController: NavController,                                   // Navegación entre pantallas
    globalUserViewModel: GlobalUserViewModel,                       // Estado global de sesión de usuario
    amigosViewModel: AmigosViewModel = viewModel(
        factory = AmigosViewModel.Factory()
    ),                                                              // VM de amigos (lista/solicitudes/acciones)
    agregarAmigoViewModel: AgregarAmigoViewModel = viewModel(
        factory = AgregarAmigoViewModel.Factory()
    ),                                                              // VM para buscar/enviar solicitud por UID
    modifier: Modifier = Modifier,
) {
    val sesionOnlineActiva by globalUserViewModel.sesionOnlineActiva.collectAsState() // Flag de sesión activa
    LaunchedEffect(Unit) { globalUserViewModel.cargarNombreUsuarioOnlineSiSesionActiva() } // Carga datos de usuario si hay sesión
    LaunchedEffect(Unit) { amigosViewModel.cargarAmigosYSolicitudes() }                    // Carga amigos y solicitudes al entrar

    val cs = MaterialTheme.colorScheme
    val gradientBorder = Brush.horizontalGradient(listOf(cs.primary, cs.secondary))
    val modernBackground = TorneoYaPalette.backgroundGradient

    // ---- Vista para usuarios sin sesión: CTA a login/registro ----
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
                Surface( // Icono grande dentro de círculo decorativo
                    shape = CircleShape,
                    color = cs.primary.copy(alpha = 0.13f),
                    shadowElevation = 0.dp,
                    modifier = Modifier.size(85.dp)
                ) {
                    Icon(
                        Icons.Default.PersonAdd,
                        contentDescription = stringResource(id = R.string.gen_icono_amigos_desc),
                        tint = cs.primary,
                        modifier = Modifier.padding(22.dp)
                    )
                }
                Spacer(Modifier.height(18.dp))
                Text( // Título y descripción de acceso
                    text = stringResource(id = R.string.amigossc_acceso_titulo),
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Black,
                    color = cs.onBackground,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    text = stringResource(id = R.string.amigossc_acceso_desc),
                    fontSize = 16.sp,
                    color = cs.onSurfaceVariant,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontWeight = FontWeight.Normal,
                )
                Spacer(Modifier.height(32.dp))

                // Botón: ir a Login
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .border(
                            width = 2.dp,
                            brush = gradientBorder,
                            shape = RoundedCornerShape(15.dp)
                        )
                        .background(
                            Brush.horizontalGradient(
                                listOf(cs.surfaceVariant, cs.surface)
                            )
                        )
                        .clickable { navController.navigate("login") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(id = R.string.gen_iniciar_sesion),
                        color = cs.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(Modifier.height(11.dp))

                // Botón: ir a Registro
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .border(
                            width = 2.dp,
                            brush = gradientBorder,
                            shape = RoundedCornerShape(15.dp)
                        )
                        .background(
                            Brush.horizontalGradient(
                                listOf(cs.surfaceVariant, cs.surface)
                            )
                        )
                        .clickable { navController.navigate("register") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(id = R.string.gen_crear_cuenta),
                        color = cs.primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }

                Spacer(Modifier.height(26.dp))

                Text( // Nota sobre modo local
                    text = stringResource(id = R.string.amigossc_cuenta_local),
                    fontSize = 14.sp,
                    color = cs.onSurfaceVariant,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        return // Fin de rama sin sesión
    }

    // ---- Estado principal con sesión ----
    val amigos by amigosViewModel.amigos.collectAsState()           // Lista de amigos
    val solicitudes by amigosViewModel.solicitudes.collectAsState() // Solicitudes pendientes
    val mensaje by amigosViewModel.mensaje.collectAsState()         // Mensajes/avisos del VM (no visible aquí)
    val agregarUiState by agregarAmigoViewModel.uiState.collectAsState() // Estado de búsqueda/solicitud
    val userUid by agregarAmigoViewModel.miUid.collectAsState()          // UID propio (para copiar/compartir)
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true) // Estado del sheet
    var showSheet by remember { mutableStateOf(false) }             // Visibilidad del sheet de añadir amigo
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var expandedUid by remember { mutableStateOf<String?>(null) }   // UID cuyo menú contextual está abierto
    var showEliminarDialog by remember { mutableStateOf(false) }    // Visibilidad del diálogo eliminar
    var uidAEliminar by remember { mutableStateOf<String?>(null) }  // UID objetivo a eliminar

    Box(
        Modifier
            .fillMaxSize()
            .background(modernBackground)
    ) {
        Column(Modifier.fillMaxSize()) {
            // ---- Cabecera con título y acceso a solicitudes ----
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
                    color = cs.onBackground,
                    modifier = Modifier.weight(1f)
                )
                Box( // Botón redondo: ir a pantalla de solicitudes (con badge si hay)
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            brush = gradientBorder,
                            shape = CircleShape
                        )
                        .background(
                            Brush.horizontalGradient(
                                listOf(cs.surfaceVariant, cs.surface)
                            )
                        )
                        .clickable { navController.navigate("solicitudes_pendientes") },
                    contentAlignment = Alignment.Center
                ) {
                    if (solicitudes.isNotEmpty()) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = cs.secondary,
                                    contentColor = cs.onSecondary,
                                ) {
                                    Text("${solicitudes.size}")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = stringResource(id = R.string.amigossc_solicitudes_desc),
                                tint = cs.secondary,
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = stringResource(id = R.string.amigossc_solicitudes_desc),
                            tint = cs.secondary,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // ---- Contenido: lista de amigos o vacío ----
            if (amigos.isEmpty()) {
                Box( // Mensaje de vacío
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(id = R.string.amigossc_no_amigos),
                        color = cs.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                }
            } else {
                var showToast by remember { mutableStateOf(false) } // Toast tras copiar

                if (showToast) {
                    LaunchedEffect(Unit) {
                        Toast.makeText(context, context.getString(R.string.gen_copiado), Toast.LENGTH_SHORT).show()
                        showToast = false
                    }
                }
                LazyColumn( // Lista de tarjetas de amigo
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 10.dp)
                ) {
                    items(amigos) { amigo ->
                        Surface( // Tarjeta de amigo: avatar + nombre + uid + menú
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp, horizontal = 6.dp)
                                .clip(RoundedCornerShape(17.dp))
                                .border(
                                    width = 2.dp,
                                    brush = gradientBorder,
                                    shape = RoundedCornerShape(17.dp)
                                )
                                .background(cs.surfaceVariant)
                                .clickable {
                                    navController.navigate("perfil_amigo/${amigo.uid}") // Ir al perfil del amigo
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
                                val avatarNum = amigo.avatar ?: 0
                                val avatarResId =
                                    if (avatarNum > 0)
                                        LocalContext.current.resources.getIdentifier(
                                            "avatar_$avatarNum",
                                            "drawable",
                                            LocalContext.current.packageName
                                        )
                                    else
                                        LocalContext.current.resources.getIdentifier(
                                            "avatar_placeholder",
                                            "drawable",
                                            LocalContext.current.packageName
                                        )

                                Box( // Contenedor del avatar (icono o inicial)
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(cs.primary.copy(alpha = 0.22f), cs.secondary.copy(alpha = 0.17f))
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
                                            color = cs.onSurface,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 21.sp
                                        )
                                    }
                                }
                                Spacer(Modifier.width(15.dp))
                                Column(Modifier.weight(1f)) { // Nombre y UID
                                    Text(
                                        text = stringResource(id = R.string.amigossc_nombre_label, amigo.nombreUsuario),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 17.sp,
                                        color = cs.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = stringResource(id = R.string.amigossc_uid_label, amigo.uid),
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.mutedText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Box { // Menú contextual (copiar UID / eliminar)
                                    IconButton(
                                        onClick = { expandedUid = amigo.uid }
                                    ) {
                                        Icon(
                                            Icons.Default.MoreHoriz,
                                            contentDescription = stringResource(id = R.string.amigossc_opciones),
                                            tint = cs.secondary
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
                                            text = { Text(stringResource(id = R.string.gen_eliminar_amigo), color = cs.error) },
                                            onClick = {
                                                uidAEliminar = amigo.uid
                                                showEliminarDialog = true
                                                expandedUid = null
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Default.Delete, contentDescription = null, tint = cs.error)
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

        // ---- Botón flotante: abrir sheet para añadir amigo por UID ----
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
                contentColor = cs.primary
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                width = 2.dp,
                brush = gradientBorder
            )
        ) {
            Icon(
                Icons.Default.PersonAdd,
                contentDescription = stringResource(id = R.string.amigossc_añadir_amigo),
                modifier = Modifier.size(24.dp),
                tint = cs.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                stringResource(id = R.string.amigossc_añadir_amigo),
                color = cs.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        // ---- Sheet para copiar tu UID y buscar/enviar solicitud ----
        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showSheet = false
                    agregarAmigoViewModel.resetUi() // Limpia estado del sheet al cerrarse
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
                                .background(cs.onSurfaceVariant, RoundedCornerShape(5.dp))
                        )
                    }
                },
                containerColor = cs.surface
            ) {
                BottomSheetContent( // Contenido del sheet (tu UID + búsqueda por UID)
                    userUid = userUid,
                    clipboard = clipboard,
                    context = context,
                    agregarUiState = agregarUiState,
                    agregarAmigoViewModel = agregarAmigoViewModel
                )
            }
        }

        // ---- Diálogo de confirmación para eliminar amigo ----
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
                            brush = gradientBorder,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .background(cs.surface)
                        .padding(26.dp)
                        .widthIn(min = 270.dp, max = 340.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(id = R.string.gen_eliminar_titulo),
                            fontWeight = FontWeight.Bold,
                            color = cs.onSurface,
                            fontSize = 19.sp
                        )
                        Spacer(Modifier.height(9.dp))
                        Text(
                            stringResource(id = R.string.amigossc_eliminar_confirm),
                            color = cs.onSurfaceVariant,
                            fontSize = 15.sp
                        )
                        Spacer(Modifier.height(22.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton( // Cancelar y cerrar
                                onClick = {
                                    showEliminarDialog = false
                                    uidAEliminar = null
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    width = 2.dp,
                                    brush = gradientBorder
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = cs.onSurface
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                            ) {
                                Text(
                                    stringResource(id = R.string.gen_cancelar),
                                    color = cs.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            OutlinedButton( // Confirmar eliminación (llama al VM)
                                onClick = {
                                    amigosViewModel.eliminarAmigo(uidAEliminar!!)
                                    showEliminarDialog = false
                                    uidAEliminar = null
                                },
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    width = 2.dp,
                                    brush = Brush.horizontalGradient(
                                        listOf(cs.error, cs.secondary)
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = cs.error
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                            ) {
                                Text(
                                    stringResource(id = R.string.gen_eliminar),
                                    color = cs.error,
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
    userUid: String?,                                              // UID propio a mostrar/copiar
    clipboard: androidx.compose.ui.platform.ClipboardManager,      // Acceso al portapapeles
    context: android.content.Context,                              // Contexto para Toast
    agregarUiState: AgregarAmigoViewModel.UiState,                 // Estado (idle/búsqueda/error/éxito)
    agregarAmigoViewModel: AgregarAmigoViewModel                   // Acciones: buscar y enviar solicitud
) {
    val cs = MaterialTheme.colorScheme
    val gradientBorder = Brush.horizontalGradient(listOf(cs.primary, cs.secondary))

    var amigoUidInput by remember { mutableStateOf("") }           // Texto introducido (UID a buscar)
    var showToast by remember { mutableStateOf(false) }            // Controla Toast de copiado

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
        // ---- Caja con tu UID y botón de copiar ----
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(
                    width = 2.dp,
                    brush = gradientBorder,
                    shape = RoundedCornerShape(10.dp)
                )
                .background(cs.surfaceVariant)
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(userUid ?: stringResource(id = R.string.gen_cargando), color = cs.onSurface, fontSize = 15.sp)
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = {
                    userUid?.let {
                        clipboard.setText(androidx.compose.ui.text.AnnotatedString(it))
                        showToast = true
                    }
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = stringResource(id = R.string.gen_copiar_uid), tint = cs.secondary)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ---- Cabecera de la sección de búsqueda por UID ----
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(
                    width = 2.dp,
                    brush = gradientBorder,
                    shape = RoundedCornerShape(10.dp)
                )
                .background(cs.surfaceVariant)
                .padding(vertical = 12.dp)
        ) {
            Text(
                stringResource(id = R.string.amigossc_buscar_uid),
                color = cs.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(Modifier.height(10.dp))

        // ---- Campo para pegar/escribir UID del amigo ----
        OutlinedTextField(
            value = amigoUidInput,
            onValueChange = { amigoUidInput = it },
            label = { Text(stringResource(id = R.string.amigossc_uid_amigo), color = cs.secondary) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp)),
            shape = RoundedCornerShape(10.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = cs.secondary,
                unfocusedBorderColor = cs.surfaceVariant,
                cursorColor = cs.onSurface,
                focusedLabelColor = cs.secondary,
                unfocusedLabelColor = cs.onSurfaceVariant,
            ),
            trailingIcon = {
                IconButton(onClick = {
                    val clip = clipboard.getText()
                    if (clip != null) amigoUidInput = clip.text
                }) {
                    Icon(Icons.Default.ContentPaste, contentDescription = stringResource(id = R.string.gen_pegar_uid), tint = cs.secondary)
                }
            }
        )
        Spacer(Modifier.height(16.dp))

        // ---- Botón de búsqueda: consulta al VM por UID ----
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
                brush = gradientBorder
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = cs.surfaceVariant,
                contentColor = cs.onSurface
            )
        ) {
            Text(stringResource(id = R.string.gen_buscar), color = cs.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        // ---- Resultados/estado de la búsqueda ----
        when (agregarUiState) {
            is AgregarAmigoViewModel.UiState.Busqueda -> {
                val usuario = (agregarUiState as AgregarAmigoViewModel.UiState.Busqueda).usuario
                Spacer(Modifier.height(22.dp))
                Box( // Tarjeta con datos del usuario encontrado y CTA para enviar solicitud
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(13.dp))
                        .border(
                            width = 2.dp,
                            brush = gradientBorder,
                            shape = RoundedCornerShape(13.dp)
                        )
                        .background(cs.surfaceVariant)
                        .padding(18.dp)
                ) {
                    Column {
                        Text(
                            stringResource(id = R.string.amigossc_nombre_label, usuario.nombreUsuario),
                            color = cs.onSurfaceVariant
                        )
                        Text(
                            stringResource(id = R.string.amigossc_uid_label, usuario.uid),
                            color = MaterialTheme.colorScheme.mutedText
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton( // Envía solicitud y limpia input
                                onClick = {
                                    agregarAmigoViewModel.enviarSolicitud(usuario.uid)
                                    amigoUidInput = ""
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    width = 2.dp,
                                    brush = gradientBorder
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = cs.surfaceVariant,
                                    contentColor = cs.onSurface
                                ),
                                modifier = Modifier.height(44.dp)
                            ) {
                                Text(
                                    stringResource(id = R.string.amigossc_si_enviar_solicitud),
                                    color = cs.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }

                        }
                    }
                }
            }
            is AgregarAmigoViewModel.UiState.Error -> {
                Spacer(Modifier.height(18.dp))
                Text( // Mensaje de error de búsqueda/solicitud
                    text = stringResource(id = (agregarUiState as AgregarAmigoViewModel.UiState.Error).resId),
                    color = cs.error
                )
            }
            is AgregarAmigoViewModel.UiState.Exito -> {
                Spacer(Modifier.height(18.dp))
                Text( // Confirmación de solicitud enviada
                    stringResource(id = R.string.amigossc_solicitud_enviada),
                    color = cs.primary
                )
            }
            else -> { /* Idle / Sin estado visible */ }
        }
        Spacer(Modifier.height(12.dp))
    }
}
