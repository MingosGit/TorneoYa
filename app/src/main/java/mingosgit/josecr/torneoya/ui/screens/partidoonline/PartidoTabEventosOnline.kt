package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mingosgit.josecr.torneoya.data.firebase.GoleadorFirebase
import mingosgit.josecr.torneoya.data.firebase.EquipoFirebase
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.R

@Composable
fun PartidoTabEventosOnline(
    partidoUid: String,
    uiState: mingosgit.josecr.torneoya.viewmodel.partidoonline.VisualizarPartidoOnlineUiState,
    reloadKey: Int = 0
) {
    val context = LocalContext.current
    val descReloadGoals = stringResource(id = R.string.ponlineeve_desc_reload_goals)
    val iconGoal = stringResource(id = R.string.ponlineeve_icon_goal)
    val iconAssist = stringResource(id = R.string.ponlineeve_icon_assist)
    val textNoEvents = stringResource(id = R.string.ponlineeve_text_no_events)

    val scope = rememberCoroutineScope()
    var eventos by remember { mutableStateOf<List<GolEvento>>(emptyList()) }
    var equipoAUid by remember { mutableStateOf<String?>(null) }
    var equipoBUid by remember { mutableStateOf<String?>(null) }
    var nombreA by remember { mutableStateOf(uiState.nombreEquipoA) }
    var nombreB by remember { mutableStateOf(uiState.nombreEquipoB) }
    var isLoading by remember { mutableStateOf(true) }
    var triggerReload by remember { mutableStateOf(0) }
    val listState = rememberLazyListState()

    fun recargar() { triggerReload++ }

    LaunchedEffect(partidoUid, reloadKey, triggerReload) {
        isLoading = true
        scope.launch {
            val db = FirebaseFirestore.getInstance()
            val snap = db.collection("partidos").document(partidoUid).get().await()
            equipoAUid = snap.getString("equipoAId")
            equipoBUid = snap.getString("equipoBId")

            equipoAUid?.let {
                val equipoA = db.collection("equipos").document(it).get().await().toObject(EquipoFirebase::class.java)
                if (equipoA?.nombre?.isNotBlank() == true) nombreA = equipoA.nombre
            }
            equipoBUid?.let {
                val equipoB = db.collection("equipos").document(it).get().await().toObject(EquipoFirebase::class.java)
                if (equipoB?.nombre?.isNotBlank() == true) nombreB = equipoB.nombre
            }

            val golesDocs = db.collection("goleadores")
                .whereEqualTo("partidoUid", partidoUid)
                .get().await()
                .documents

            val goles = golesDocs.mapNotNull { it.toObject(GoleadorFirebase::class.java)?.copy(uid = it.id) }

            val jugadorUids = goles.mapNotNull { it.jugadorUid.takeIf { u -> !u.isNullOrBlank() } } +
                    goles.mapNotNull { it.asistenciaJugadorUid.takeIf { u -> !u.isNullOrBlank() } }
            val jugadoresDocs = jugadorUids.distinct()
                .filter { it.isNotBlank() }
                .mapNotNull { uid ->
                    val jugSnap = db.collection("jugadores").document(uid).get().await()
                    if (jugSnap.exists()) uid to (jugSnap.getString("nombre") ?: "") else null
                }
            val faltantes = jugadorUids.distinct().filter { it.isNotBlank() && jugadoresDocs.none { pair -> pair.first == it } }
            val usuariosDocs = faltantes.mapNotNull { uid ->
                val userSnap = db.collection("usuarios").document(uid).get().await()
                if (userSnap.exists()) uid to (userSnap.getString("nombreUsuario") ?: "") else null
            }
            val nombresMap = (jugadoresDocs + usuariosDocs).toMap()

            eventos = goles
                .sortedBy { it.minuto ?: Int.MIN_VALUE }
                .map { gol ->
                    val nombreJugador = when {
                        !gol.jugadorUid.isNullOrBlank() -> {
                            nombresMap[gol.jugadorUid]
                                ?: gol.jugadorNombreManual
                                ?: golesDocs.find { d -> d.id == gol.uid }?.getString("jugadorManual")
                                ?: textNoEvents
                        }
                        !gol.jugadorNombreManual.isNullOrBlank() -> gol.jugadorNombreManual
                        else -> golesDocs.find { d -> d.id == gol.uid }?.getString("jugadorManual") ?: textNoEvents
                    }
                    val nombreAsistente = when {
                        !gol.asistenciaJugadorUid.isNullOrBlank() -> {
                            nombresMap[gol.asistenciaJugadorUid]
                                ?: gol.asistenciaNombreManual
                                ?: golesDocs.find { d -> d.id == gol.uid }?.getString("asistenciaManual")
                                ?: textNoEvents
                        }
                        !gol.asistenciaNombreManual.isNullOrBlank() -> gol.asistenciaNombreManual
                        else -> golesDocs.find { d -> d.id == gol.uid }?.getString("asistenciaManual")
                    }
                    GolEvento(
                        equipoUid = gol.equipoUid,
                        jugador = nombreJugador ?: textNoEvents,
                        minuto = gol.minuto,
                        asistente = if (!nombreAsistente.isNullOrBlank()) nombreAsistente else null
                    )
                }
            isLoading = false
        }
    }

    val eventosSize = eventos.size
    val oldEventosSize = remember { mutableStateOf(0) }
    LaunchedEffect(eventosSize) {
        if (eventosSize > oldEventosSize.value) {
            listState.animateScrollToItem(0)
        }
        oldEventosSize.value = eventosSize
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = { recargar() }
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = descReloadGoals,
                    tint = TorneoYaPalette.blue
                )
            }
        }

        if (isLoading) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Column
        }

        if (eventos.isEmpty()) {
            Text(
                text = textNoEvents,
                style = MaterialTheme.typography.bodyMedium,
                color = TorneoYaPalette.mutedText,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            state = listState
        ) {
            items(eventos) { evento ->
                val isEquipoA = evento.equipoUid == equipoAUid

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isEquipoA) {
                        Box(
                            modifier = Modifier
                                .weight(4f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    brush = Brush.horizontalGradient(listOf(TorneoYaPalette.blue.copy(alpha = 0.18f), Color.Transparent)),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .border(
                                    width = 2.dp,
                                    brush = Brush.horizontalGradient(listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(vertical = 11.dp, horizontal = 16.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.Start
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = iconGoal,
                                        fontSize = 20.sp,
                                        modifier = Modifier.padding(end = 6.dp)
                                    )
                                    Text(
                                        text = evento.jugador,
                                        fontWeight = FontWeight.Bold,
                                        color = TorneoYaPalette.blue,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                }
                                if (!evento.asistente.isNullOrBlank()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Text(text = iconAssist, fontSize = 16.sp, modifier = Modifier.padding(end = 4.dp))
                                        Text(
                                            text = evento.asistente!!,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TorneoYaPalette.accent
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(4f))
                    }

                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        evento.minuto?.let {
                            Text(
                                text = "${it}'",
                                color = TorneoYaPalette.mutedText,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                                modifier = Modifier
                                    .background(
                                        color = Color(0x1A3B4252),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (!isEquipoA) {
                        Box(
                            modifier = Modifier
                                .weight(4f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    brush = Brush.horizontalGradient(listOf(Color.Transparent, TorneoYaPalette.violet.copy(alpha = 0.18f))),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .border(
                                    width = 2.dp,
                                    brush = Brush.horizontalGradient(listOf(TorneoYaPalette.violet, TorneoYaPalette.blue)),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(vertical = 11.dp, horizontal = 16.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = evento.jugador,
                                        fontWeight = FontWeight.Bold,
                                        color = TorneoYaPalette.violet,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                    Text(
                                        text = iconGoal,
                                        fontSize = 20.sp,
                                        modifier = Modifier.padding(start = 6.dp)
                                    )
                                }
                                if (!evento.asistente.isNullOrBlank()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.End,
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .fillMaxWidth()
                                    ) {
                                        Text(
                                            text = evento.asistente!!,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TorneoYaPalette.accent
                                        )
                                        Text(text = iconAssist, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
                                    }
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(4f))
                    }
                }
            }
        }
    }
}

data class GolEvento(
    val equipoUid: String,
    val jugador: String,
    val minuto: Int?,
    val asistente: String?
)
