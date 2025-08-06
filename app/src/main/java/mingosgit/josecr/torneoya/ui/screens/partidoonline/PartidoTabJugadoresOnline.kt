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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.viewmodel.partidoonline.VisualizarPartidoOnlineUiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun PartidoTabJugadoresOnline(
    uiState: VisualizarPartidoOnlineUiState
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Cambia offset dependiendo de si es equipo A o B
        EquipoColumnWithFriendship(
            nombreEquipo = uiState.nombreEquipoA,
            jugadores = uiState.jugadoresEquipoA,
            borderBrush = Brush.horizontalGradient(
                listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
            ),
            dropdownOffset = DpOffset((-140).dp, (-10).dp), // Izquierda, sale al lado izquierdo
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
            dropdownOffset = DpOffset(180.dp, (-10).dp), // Derecha, sale al lado derecho
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
    dropdownOffset: DpOffset, // NUEVO
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var expandedIndex by remember { mutableStateOf<Int?>(null) }
    var mensajeDialog by remember { mutableStateOf<String?>(null) }
    var sendingSolicitud by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = nombreEquipo,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TorneoYaPalette.violet,
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
                .background(TorneoYaPalette.violet)
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
                                    Color(0xFF23273D),
                                    Color(0xFF1C1D25)
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
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    // MENÚ ESTILO CONTEXTUAL TIPO DROPDOWN AL LADO DEL JUGADOR
                    if (expandedIndex == idx) {
                        AmistadDropdownMenu(
                            borderBrush = borderBrush,
                            offset = dropdownOffset,
                            onDismissRequest = { expandedIndex = null },
                            enabled = !sendingSolicitud,
                            onClick = {
                                sendingSolicitud = true
                                scope.launch {
                                    val res = enviarSolicitudAmistadSiProcede(jugadorNombre, context)
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
                        text = "Sin jugadores asignados",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    // Diálogo de confirmación o error tras enviar solicitud
    if (mensajeDialog != null) {
        AlertDialog(
            onDismissRequest = { mensajeDialog = null },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .border(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(
                                listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
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
                            "OK",
                            color = TorneoYaPalette.blue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            title = {
                Text(
                    "Amigos",
                    color = TorneoYaPalette.violet,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    mensajeDialog ?: "",
                    color = Color.White,
                    fontSize = 16.sp
                )
            },
            shape = RoundedCornerShape(18.dp),
            containerColor = Color(0xFF23273D),
            modifier = Modifier
                .border(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(
                        listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
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
                    listOf(Color(0xFF222441), Color(0xFF242348))
                ),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    "Solicitar amistad",
                    color = TorneoYaPalette.blue,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            },
            onClick = onClick,
            enabled = enabled
        )
    }
}

suspend fun enviarSolicitudAmistadSiProcede(jugadorNombre: String, context: android.content.Context): String {
    val auth = FirebaseAuth.getInstance()
    val miUid = auth.currentUser?.uid ?: return "Debes estar logueado para enviar solicitudes."
    val db = FirebaseFirestore.getInstance()

    try {
        val usersQuery = db.collection("usuarios").whereEqualTo("nombreUsuario", jugadorNombre).get().await()
        val localQuery = db.collection("jugadores").whereEqualTo("nombre", jugadorNombre).get().await()
        val isLocal = localQuery.documents.any { it.exists() }
        val userDoc = usersQuery.documents.firstOrNull()
        val uidDestino = userDoc?.getString("uid")

        if (isLocal && (uidDestino == null || uidDestino.isBlank())) {
            return "Este jugador es local y no tiene cuenta online."
        }
        if (uidDestino == miUid) {
            return "No puedes enviarte una solicitud a ti mismo."
        }
        if (uidDestino == null) {
            return "No se encontró un usuario online con ese nombre."
        }
        val amigoSnap = db.collection("usuarios").document(miUid).collection("amigos").document(uidDestino).get().await()
        if (amigoSnap.exists()) {
            return "¡Este usuario ya es tu amigo!"
        }
        val solicitudSnap = db.collection("usuarios").document(uidDestino).collection("solicitudes_amistad").document(miUid).get().await()
        if (solicitudSnap.exists()) {
            return "Ya enviaste una solicitud de amistad a este usuario."
        }
        val miUsuarioSnap = db.collection("usuarios").document(miUid).get().await()
        val miUsuario = miUsuarioSnap.data ?: return "Error al recuperar tu usuario."
        db.collection("usuarios").document(uidDestino)
            .collection("solicitudes_amistad").document(miUid).set(miUsuario).await()
        return "¡Solicitud de amistad enviada a $jugadorNombre!"
    } catch (e: Exception) {
        return "Error enviando solicitud: ${e.localizedMessage ?: e.toString()}"
    }
}
