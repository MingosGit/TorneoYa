package mingosgit.josecr.torneoya.ui.screens.partidoonline

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.ui.theme.mutedText
import mingosgit.josecr.torneoya.viewmodel.partidoonline.VisualizarPartidoOnlineUiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import mingosgit.josecr.torneoya.R

@Composable
fun PartidoTabJugadoresOnline(
    uiState: VisualizarPartidoOnlineUiState
) {
    val cs = MaterialTheme.colorScheme
    val textNoPlayers = stringResource(id = R.string.ponlinejug_text_no_players)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        EquipoColumnWithFriendship(
            nombreEquipo = uiState.nombreEquipoA,
            jugadores = uiState.jugadoresEquipoA,
            borderBrush = Brush.horizontalGradient(
                listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
            ),
            dropdownOffset = DpOffset((-140).dp, (-10).dp),
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        )
        EquipoColumnWithFriendship(
            nombreEquipo = uiState.nombreEquipoB,
            jugadores = uiState.jugadoresEquipoB,
            borderBrush = Brush.horizontalGradient(
                listOf(TorneoYaPalette.accent, TorneoYaPalette.violet)
            ),
            dropdownOffset = DpOffset(180.dp, (-10).dp),
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EquipoColumnWithFriendship(
    nombreEquipo: String,
    jugadores: List<String>,
    borderBrush: Brush,
    dropdownOffset: DpOffset,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var expandedIndex by remember { mutableStateOf<Int?>(null) }
    var mensajeDialog by remember { mutableStateOf<String?>(null) }
    var sendingSolicitud by remember { mutableStateOf(false) }

    val textNoPlayers = stringResource(id = R.string.ponlinejug_text_no_players)
    val btnOk = stringResource(id = R.string.ponlinejug_btn_ok)
    val titleFriends = stringResource(id = R.string.ponlinejug_title_friends)
    val menuRequestFriendship = stringResource(id = R.string.ponlinejug_menu_request_friendship)
    val msgMustBeLoggedIn = stringResource(id = R.string.ponlinejug_msg_must_be_logged_in)
    val msgLocalPlayerNoAccount = stringResource(id = R.string.ponlinejug_msg_local_player_no_account)
    val msgCannotSendToSelf = stringResource(id = R.string.ponlinejug_msg_cannot_send_to_self)
    val msgUserNotFound = stringResource(id = R.string.ponlinejug_msg_user_not_found)
    val msgAlreadyFriend = stringResource(id = R.string.ponlinejug_msg_already_friend)
    val msgRequestAlreadySent = stringResource(id = R.string.ponlinejug_msg_request_already_sent)
    val msgErrorRetrievingUser = stringResource(id = R.string.ponlinejug_msg_error_retrieving_user)
    val msgRequestSent = stringResource(id = R.string.ponlinejug_msg_request_sent)
    val msgErrorSendingRequest = stringResource(id = R.string.ponlinejug_msg_error_sending_request)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = nombreEquipo,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = cs.primary,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
        )
        Divider(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .height(2.dp)
                .background(cs.primary)
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(jugadores) { idx, jugadorNombre ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp, horizontal = 10.dp)
                        .shadow(2.dp, RoundedCornerShape(13.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    cs.surface,
                                    cs.background
                                )
                            ),
                            shape = RoundedCornerShape(13.dp)
                        )
                        .border(
                            width = 2.dp,
                            brush = borderBrush,
                            shape = RoundedCornerShape(13.dp)
                        )
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                expandedIndex = idx
                            }
                        )
                        .padding(vertical = 9.dp, horizontal = 2.dp)
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                    ) {
                        Text(
                            text = jugadorNombre,
                            fontSize = 16.sp,
                            color = cs.onSurface,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    if (expandedIndex == idx) {
                        AmistadDropdownMenu(
                            borderBrush = borderBrush,
                            offset = dropdownOffset,
                            onDismissRequest = { expandedIndex = null },
                            enabled = !sendingSolicitud,
                            onClick = {
                                sendingSolicitud = true
                                scope.launch {
                                    val res = enviarSolicitudAmistadSiProcede(
                                        jugadorNombre, context,
                                        msgMustBeLoggedIn,
                                        msgLocalPlayerNoAccount,
                                        msgCannotSendToSelf,
                                        msgUserNotFound,
                                        msgAlreadyFriend,
                                        msgRequestAlreadySent,
                                        msgErrorRetrievingUser,
                                        msgRequestSent,
                                        msgErrorSendingRequest
                                    )
                                    mensajeDialog = res
                                    sendingSolicitud = false
                                    expandedIndex = null
                                }
                            }
                        )
                    }
                }
            }
            if (jugadores.isEmpty()) {
                item {
                    Text(
                        text = textNoPlayers,
                        fontSize = 14.sp,
                        color = cs.mutedText,
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    if (mensajeDialog != null) {
        AlertDialog(
            onDismissRequest = { mensajeDialog = null },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .border(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(
                                listOf(cs.primary, cs.secondary)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    TextButton(
                        onClick = { mensajeDialog = null },
                        modifier = Modifier
                            .defaultMinSize(minWidth = 80.dp)
                            .background(Color.Transparent, shape = RoundedCornerShape(10.dp))
                    ) {
                        Text(
                            btnOk,
                            color = cs.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            title = {
                Text(
                    titleFriends,
                    color = cs.secondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    mensajeDialog ?: "",
                    color = cs.onSurface,
                    fontSize = 16.sp
                )
            },
            shape = RoundedCornerShape(18.dp),
            containerColor = cs.surface,
            modifier = Modifier
                .border(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(
                        listOf(cs.primary, cs.secondary)
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
        )
    }
}

@Composable
private fun AmistadDropdownMenu(
    borderBrush: Brush,
    offset: DpOffset = DpOffset.Zero,
    onDismissRequest: () -> Unit,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val menuText = stringResource(id = R.string.ponlinejug_menu_request_friendship)
    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismissRequest,
        offset = offset,
        modifier = Modifier
            .border(
                2.dp,
                borderBrush,
                RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.horizontalGradient(
                    listOf(cs.surfaceVariant, cs.surface)
                ),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    menuText,
                    color = cs.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            },
            onClick = onClick,
            enabled = enabled
        )
    }
}

suspend fun enviarSolicitudAmistadSiProcede(
    jugadorNombre: String,
    context: android.content.Context,
    msgMustBeLoggedIn: String,
    msgLocalPlayerNoAccount: String,
    msgCannotSendToSelf: String,
    msgUserNotFound: String,
    msgAlreadyFriend: String,
    msgRequestAlreadySent: String,
    msgErrorRetrievingUser: String,
    msgRequestSent: String,
    msgErrorSendingRequest: String
): String {
    val auth = FirebaseAuth.getInstance()
    val miUid = auth.currentUser?.uid ?: return msgMustBeLoggedIn
    val db = FirebaseFirestore.getInstance()

    try {
        val usersQuery = db.collection("usuarios").whereEqualTo("nombreUsuario", jugadorNombre).get().await()
        val localQuery = db.collection("jugadores").whereEqualTo("nombre", jugadorNombre).get().await()
        val isLocal = localQuery.documents.any { it.exists() }
        val userDoc = usersQuery.documents.firstOrNull()
        val uidDestino = userDoc?.getString("uid")

        if (isLocal && (uidDestino == null || uidDestino.isBlank())) {
            return msgLocalPlayerNoAccount
        }
        if (uidDestino == miUid) {
            return msgCannotSendToSelf
        }
        if (uidDestino == null) {
            return msgUserNotFound
        }
        val amigoSnap = db.collection("usuarios").document(miUid).collection("amigos").document(uidDestino).get().await()
        if (amigoSnap.exists()) {
            return msgAlreadyFriend
        }
        val solicitudSnap = db.collection("usuarios").document(uidDestino).collection("solicitudes_amistad").document(miUid).get().await()
        if (solicitudSnap.exists()) {
            return msgRequestAlreadySent
        }
        val miUsuarioSnap = db.collection("usuarios").document(miUid).get().await()
        val miUsuario = miUsuarioSnap.data ?: return msgErrorRetrievingUser
        db.collection("usuarios").document(uidDestino)
            .collection("solicitudes_amistad").document(miUid).set(miUsuario).await()
        return String.format(msgRequestSent, jugadorNombre)
    } catch (e: Exception) {
        return String.format(msgErrorSendingRequest, e.localizedMessage ?: e.toString())
    }
}
