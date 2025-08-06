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
    val btnOk = stringResource(id = R.string.gen_cerrar) // Usamos el string genérico "Cerrar" para OK

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
                            if (!esCreador) {
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
                        .padding(top = innerPadding.calculateTopPadding()) // SOLO TOP

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

                    if (showPermisoDialog) {
                        AlertDialog(
                            onDismissRequest = { showPermisoDialog = false },
                            confirmButton = {
                                TextButton(onClick = { showPermisoDialog = false }) {
                                    Text(btnOk, color = Color(0xFF8F5CFF), fontWeight = FontWeight.Bold)
                                }
                            },
                            title = { Text(dialogNoPermTitle, color = Color.White, fontWeight = FontWeight.Black) },
                            text = { Text(dialogNoPermMsg, color = Color(0xFFB7B7D1)) },
                            containerColor = Color(0xFF23273D),
                            shape = RoundedCornerShape(17.dp)
                        )
                    }

                    if (showDejarDeVerDialog) {
                        AlertDialog(
                            onDismissRequest = { showDejarDeVerDialog = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    vm.dejarDeVerPartido(usuarioUid) {
                                        showDejarDeVerDialog = false
                                        navController.popBackStack()
                                    }
                                }) {
                                    Text(dialogStopViewingConfirm, color = Color(0xFFFF7675), fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDejarDeVerDialog = false }) {
                                    Text(btnOk, color = Color(0xFF8F5CFF), fontWeight = FontWeight.Bold)
                                }
                            },
                            title = { Text(dialogStopViewingTitle, color = Color.White, fontWeight = FontWeight.Black) },
                            text = { Text(dialogStopViewingMsg, color = Color(0xFFB7B7D1)) },
                            containerColor = Color(0xFF23273D),
                            shape = RoundedCornerShape(17.dp)
                        )
                    }
                }
            }
        )
    }
}
