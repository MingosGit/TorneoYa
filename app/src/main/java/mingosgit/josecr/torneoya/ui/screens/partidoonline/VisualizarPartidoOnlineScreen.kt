package mingosgit.josecr.torneoya.ui.screens.partidoonline

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.viewmodel.partidoonline.VisualizarPartidoOnlineViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualizarPartidoOnlineScreen(
    partidoUid: String,
    navController: NavController,
    vm: VisualizarPartidoOnlineViewModel,
    usuarioUid: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showCopiedMessage by remember { mutableStateOf(false) }
    var showPermisoDialog by remember { mutableStateOf(false) }
    var showDejarDeVerDialog by remember { mutableStateOf(false) }
    val eliminado by vm.eliminado.collectAsState()
    val uiState by vm.uiState.collectAsState()

    var esCreador by remember { mutableStateOf(false) }
    var esAdmin by remember { mutableStateOf(false) }

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

    LaunchedEffect(partidoUid, usuarioUid) {
        val firestore = FirebaseFirestore.getInstance()
        val snap = firestore.collection("partidos").document(partidoUid).get().await()
        val creadorUid = snap.getString("creadorUid") ?: ""
        val administradores = (snap.get("administradores") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        esCreador = usuarioUid == creadorUid
        esAdmin = (usuarioUid == creadorUid) || administradores.contains(usuarioUid)
    }

    LaunchedEffect(partidoUid) { vm.cargarDatos(usuarioUid) }

    LaunchedEffect(eliminado) {
        if (eliminado) {
            navController.popBackStack()
        }
    }

    val modernBackground = Brush.verticalGradient(
        0.0f to Color(0xFF1B1D29),
        0.28f to Color(0xFF212442),
        0.58f to Color(0xFF191A23),
        1.0f to Color(0xFF14151B)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(modernBackground)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    actions = {
                        Row(
                            modifier = Modifier.padding(end = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
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
                                        ),
                                        shape = CircleShape
                                    ),
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
                                    tint = Color(0xFF8F5CFF),
                                    modifier = Modifier.size(25.dp)
                                )
                            }
                            IconButton(
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
                                        ),
                                        shape = CircleShape
                                    ),
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
                                    tint = Color(0xFF8F5CFF),
                                    modifier = Modifier.size(25.dp)
                                )
                            }
                            // AHORA TAMBIÉN SE MUESTRA PARA ADMIN (INCLUYE CREADOR)
                            if (esAdmin || !esCreador) {
                                IconButton(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = 2.dp,
                                            brush = Brush.horizontalGradient(
                                                listOf(Color(0xFFFF7675), TorneoYaPalette.violet)
                                            ),
                                            shape = CircleShape
                                        )
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                                            ),
                                            shape = CircleShape
                                        ),
                                    onClick = { showDejarDeVerDialog = true }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = descStopViewing,
                                        tint = Color(0xFFFF7675),
                                        modifier = Modifier.size(25.dp)
                                    )
                                }
                            }
                        }
                    }
                )
            },
            content = { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding())
                ) {
                    VisualizarPartidoOnlineContent(
                        modifier = Modifier.fillMaxSize(),
                        uiState = uiState,
                        vm = vm,
                        usuarioUid = usuarioUid,
                        partidoUid = partidoUid
                    )

                    AnimatedVisibility(
                        visible = showCopiedMessage,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Snackbar(
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.CenterHorizontally),
                            containerColor = Color(0xFF23273D),
                            contentColor = Color.White,
                            shape = RoundedCornerShape(17.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.gen_uid_copiado),
                                color = Color(0xFFB7B7D1),
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        )

        // ----------- POPUP PERMISOS -----------
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
                            brush = Brush.horizontalGradient(
                                listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                            ),
                            shape = RoundedCornerShape(22.dp)
                        )
                        .background(Color(0xFF23273D), shape = RoundedCornerShape(22.dp))
                        .padding(horizontal = 24.dp, vertical = 26.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = dialogNoPermTitle,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 21.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = dialogNoPermMsg,
                            color = Color(0xFFB7B7D1),
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(23.dp))
                        Button(
                            onClick = { showPermisoDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                            shape = RoundedCornerShape(13.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                2.dp,
                                Brush.horizontalGradient(
                                    listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                                )
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Text(
                                text = btnOk,
                                color = TorneoYaPalette.blue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
        // ----------- FIN POPUP PERMISOS -----------

        // ----------- POPUP DEJAR DE VISUALIZAR -----------
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
                            brush = Brush.horizontalGradient(
                                listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                            ),
                            shape = RoundedCornerShape(22.dp)
                        )
                        .background(Color(0xFF23273D), shape = RoundedCornerShape(22.dp))
                        .padding(horizontal = 24.dp, vertical = 26.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = dialogStopViewingTitle,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 21.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = dialogStopViewingMsg,
                            color = Color(0xFFB7B7D1),
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(23.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    if (esAdmin) {
                                        // Admin/creador: solo abandonar la pantalla (no se toca la lista de acceso)
                                        showDejarDeVerDialog = false
                                        navController.popBackStack()
                                    } else {
                                        // Usuario con acceso: se le quita el acceso y se sale
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
                                border = androidx.compose.foundation.BorderStroke(
                                    2.dp,
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFF7675), TorneoYaPalette.violet)
                                    )
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                            ) {
                                Text(
                                    text = dialogStopViewingConfirm,
                                    color = Color(0xFFFF7675),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            OutlinedButton(
                                onClick = { showDejarDeVerDialog = false },
                                modifier = Modifier.weight(1f),
                                border = androidx.compose.foundation.BorderStroke(
                                    2.dp,
                                    Brush.horizontalGradient(
                                        listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                                    )
                                ),
                                shape = RoundedCornerShape(13.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = TorneoYaPalette.blue
                                ),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    btnOk,
                                    color = TorneoYaPalette.blue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
        // ----------- FIN POPUP DEJAR DE VISUALIZAR -----------
    }
}
