package mingosgit.josecr.torneoya.ui.screens.partidoonline.visualizacion

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.viewmodel.partidoonline.VisualizarPartidoOnlineViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import mingosgit.josecr.torneoya.ui.theme.mutedText
import mingosgit.josecr.torneoya.ui.theme.text

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// Pantalla principal que muestra la visualización del partido online y acciones (compartir UID, administrar, dejar de ver).
fun VisualizarPartidoOnlineScreen(
    partidoUid: String,
    navController: NavController,
    vm: VisualizarPartidoOnlineViewModel,
    usuarioUid: String
) {
    // Contexto y scope para corrutinas.
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estados locales para mostrar snackbars y diálogos.
    var showCopiedMessage by remember { mutableStateOf(false) }
    var showPermisoDialog by remember { mutableStateOf(false) }
    var showDejarDeVerDialog by remember { mutableStateOf(false) }
    var showEsCreadorNoSalirDialog by remember { mutableStateOf(false) }

    // Estados del ViewModel.
    val eliminado by vm.eliminado.collectAsState()
    val uiState by vm.uiState.collectAsState()

    // Flags de rol del usuario en el partido.
    var esCreador by remember { mutableStateOf(false) }
    var esAdmin by remember { mutableStateOf(false) }

    // Strings de recursos.
    val title = stringResource(id = R.string.ponline_screen_title)
    val descShareUid = stringResource(id = R.string.ponline_desc_share_uid)
    val descAdminPartido = stringResource(id = R.string.ponline_desc_admin_partido)
    val descStopViewing = stringResource(id = R.string.ponline_desc_stop_viewing)

    val dialogNoPermTitle = stringResource(id = R.string.ponline_dialog_no_permissions_title)
    val dialogNoPermMsg = stringResource(id = R.string.ponline_dialog_no_permissions_message)
    val dialogStopViewingTitle = stringResource(id = R.string.ponline_dialog_stop_viewing_title)
    val dialogStopViewingMsg = stringResource(id = R.string.ponline_dialog_stop_viewing_message)
    val dialogStopViewingConfirm = stringResource(id = R.string.ponline_dialog_stop_viewing_confirm)
    val btnOk = stringResource(id = R.string.gen_cerrar)
    val dialogEsCreadorNoSalirTitle = stringResource(id = R.string.parequban_avisocreador)
    val dialogEsCreadorNoSalirMsg = stringResource(id = R.string.parequeban_no_puedes)

    // Carga inicial de roles del usuario (creador/admin) desde Firestore.
    LaunchedEffect(partidoUid, usuarioUid) {
        val firestore = FirebaseFirestore.getInstance()
        val snap = firestore.collection("partidos").document(partidoUid).get().await()
        val creadorUid = snap.getString("creadorUid") ?: ""
        val administradores = (snap.get("administradores") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        esCreador = usuarioUid == creadorUid
        esAdmin = (usuarioUid == creadorUid) || administradores.contains(usuarioUid)
    }

    // Dispara la carga de datos del partido para el usuario actual.
    LaunchedEffect(partidoUid) { vm.cargarDatos(usuarioUid) }

    // Si el partido se elimina, vuelve atrás en la navegación.
    LaunchedEffect(eliminado) {
        if (eliminado) {
            navController.popBackStack()
        }
    }

    // Colores y pinceles para bordes con degradado.
    val cs = MaterialTheme.colorScheme
    val gradientBorderPrimary = Brush.horizontalGradient(
        listOf(cs.primary, cs.secondary)
    )
    val gradientBorderDestructive = Brush.horizontalGradient(
        listOf(cs.error, cs.secondary)
    )

    // Contenedor raíz con fondo degradado de la app.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TorneoYaPalette.backgroundGradient)
    ) {
        // Estructura de pantalla con barra superior y contenido.
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                // Barra superior con título y acciones.
                TopAppBar(
                    title = {
                        // Título de la pantalla.
                        Text(
                            text = title,
                            color = cs.text,
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = cs.onSurface,
                        navigationIconContentColor = cs.onSurface,
                        actionIconContentColor = cs.onSurface
                    ),
                    actions = {
                        // Grupo de botones de acción: compartir, ajustes, salir/dejar de ver.
                        Row(
                            modifier = Modifier.padding(end = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Botón compartir: copia el UID del partido al portapapeles y muestra snackbar.
                            IconButton(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 2.dp,
                                        brush = gradientBorderPrimary,
                                        shape = CircleShape
                                    )
                                    .background(cs.surfaceVariant, shape = CircleShape),
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText(title, partidoUid)
                                    clipboard.setPrimaryClip(clip)
                                    showCopiedMessage = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = descShareUid,
                                    tint = cs.secondary,
                                    modifier = Modifier.size(25.dp)
                                )
                            }
                            // Botón ajustes: verifica permiso (creador/admin) y navega a administrar; si no, muestra diálogo sin permisos.
                            IconButton(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 2.dp,
                                        brush = gradientBorderPrimary,
                                        shape = CircleShape
                                    )
                                    .background(cs.surfaceVariant, shape = CircleShape),
                                onClick = {
                                    scope.launch {
                                        val firestore = FirebaseFirestore.getInstance()
                                        val snap = firestore.collection("partidos").document(partidoUid).get().await()
                                        val creadorUid = snap.getString("creadorUid") ?: ""
                                        val administradores = (snap.get("administradores") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                                        val puedeEntrar = (usuarioUid == creadorUid) || administradores.contains(usuarioUid)
                                        if (puedeEntrar) {
                                            navController.navigate("administrar_partido_online/$partidoUid")
                                        } else {
                                            showPermisoDialog = true
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = descAdminPartido,
                                    tint = cs.secondary,
                                    modifier = Modifier.size(25.dp)
                                )
                            }
                            // Botón salir/dejar de ver: cambia según sea creador o no.
                            if (!esCreador) {
                                // Usuario NO creador: permite dejar de visualizar (abre diálogo de confirmación).
                                IconButton(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = 2.dp,
                                            brush = gradientBorderDestructive,
                                            shape = CircleShape
                                        )
                                        .background(cs.surfaceVariant, shape = CircleShape),
                                    onClick = { showDejarDeVerDialog = true }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = descStopViewing,
                                        tint = cs.error,
                                        modifier = Modifier.size(25.dp)
                                    )
                                }
                            } else {
                                // Usuario creador: muestra aviso de que no puede salir.
                                IconButton(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = 2.dp,
                                            brush = gradientBorderDestructive,
                                            shape = CircleShape
                                        )
                                        .background(cs.surfaceVariant, shape = CircleShape),
                                    onClick = { showEsCreadorNoSalirDialog = true }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = descStopViewing,
                                        tint = cs.error,
                                        modifier = Modifier.size(25.dp)
                                    )
                                }
                            }
                        }
                    }
                )
            },
            content = { innerPadding ->
                // Contenido principal: datos del partido + snackbar de UID copiado.
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding())
                ) {
                    // Sección que pinta el contenido del partido (marcador, eventos, etc.). Se delega a otro composable.
                    VisualizarPartidoOnlineContent(
                        modifier = Modifier.fillMaxSize(),
                        uiState = uiState,
                        vm = vm,
                        usuarioUid = usuarioUid,
                        partidoUid = partidoUid
                    )

                    // Snackbar animado que informa de que el UID se ha copiado.
                    AnimatedVisibility(
                        visible = showCopiedMessage,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Snackbar(
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.CenterHorizontally),
                            containerColor = cs.surface,
                            contentColor = cs.onSurface,
                            shape = RoundedCornerShape(17.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.gen_uid_copiado),
                                color = cs.mutedText,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        )

        // Diálogo: sin permisos para administrar el partido.
        if (showPermisoDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight()
                        .border(
                            width = 2.dp,
                            brush = gradientBorderPrimary,
                            shape = RoundedCornerShape(22.dp)
                        )
                        .background(cs.surface, shape = RoundedCornerShape(22.dp))
                        .padding(horizontal = 24.dp, vertical = 26.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(dialogNoPermTitle, color = cs.text, fontWeight = FontWeight.Black, fontSize = 21.sp)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(dialogNoPermMsg, color = cs.mutedText, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(23.dp))
                        Button(
                            onClick = { showPermisoDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                            shape = RoundedCornerShape(13.dp),
                            border = BorderStroke(2.dp, gradientBorderPrimary),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Text(btnOk, color = cs.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        // Diálogo: el creador no puede salir de la visualización.
        if (showEsCreadorNoSalirDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight()
                        .border(
                            width = 2.dp,
                            brush = gradientBorderDestructive,
                            shape = RoundedCornerShape(22.dp)
                        )
                        .background(cs.surface, shape = RoundedCornerShape(22.dp))
                        .padding(horizontal = 24.dp, vertical = 26.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = dialogEsCreadorNoSalirMsg,
                            color = cs.text,
                            fontWeight = FontWeight.Black,
                            fontSize = 21.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = dialogEsCreadorNoSalirTitle,
                            color = cs.mutedText,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(23.dp))
                        Button(
                            onClick = { showEsCreadorNoSalirDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                            shape = RoundedCornerShape(13.dp),
                            border = BorderStroke(2.dp, gradientBorderDestructive),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Text(btnOk, color = cs.error, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        // Diálogo: dejar de visualizar el partido (confirmación). Si es admin, solo sale; si no, se avisa al VM para dejar de ver.
        if (showDejarDeVerDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight()
                        .border(
                            width = 2.dp,
                            brush = gradientBorderPrimary,
                            shape = RoundedCornerShape(22.dp)
                        )
                        .background(cs.surface, shape = RoundedCornerShape(22.dp))
                        .padding(horizontal = 24.dp, vertical = 26.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(dialogStopViewingTitle, color = cs.text, fontWeight = FontWeight.Black, fontSize = 21.sp)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(dialogStopViewingMsg, color = cs.mutedText, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(23.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Confirmar dejar de ver: si es admin, solo navega atrás; si no, llama a vm.dejarDeVerPartido.
                            Button(
                                onClick = {
                                    if (esAdmin) {
                                        showDejarDeVerDialog = false
                                        navController.popBackStack()
                                    } else {
                                        vm.dejarDeVerPartido(usuarioUid) {
                                            showDejarDeVerDialog = false
                                            navController.popBackStack()
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                                shape = RoundedCornerShape(13.dp),
                                border = BorderStroke(2.dp, gradientBorderDestructive),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                            ) {
                                Text(dialogStopViewingConfirm, color = cs.error, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            // Cancelar: cierra el diálogo.
                            OutlinedButton(
                                onClick = { showDejarDeVerDialog = false },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(2.dp, gradientBorderPrimary),
                                shape = RoundedCornerShape(13.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = cs.primary
                                ),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
                            ) {
                                Text(btnOk, color = cs.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
