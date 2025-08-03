package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mingosgit.josecr.torneoya.data.firebase.GoleadorFirebase
import mingosgit.josecr.torneoya.data.firebase.EquipoFirebase
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette

@Composable
fun PartidoTabEventosOnline(
    partidoUid: String,
    uiState: mingosgit.josecr.torneoya.viewmodel.partidoonline.VisualizarPartidoOnlineUiState,
    reloadKey: Int = 0
) {
    val scope = rememberCoroutineScope()
    var eventos by remember { mutableStateOf<List<GolEvento>>(emptyList()) }
    var equipoAUid by remember { mutableStateOf<String?>(null) }
    var equipoBUid by remember { mutableStateOf<String?>(null) }
    var nombreA by remember { mutableStateOf(uiState.nombreEquipoA) }
    var nombreB by remember { mutableStateOf(uiState.nombreEquipoB) }
    var isLoading by remember { mutableStateOf(true) }
    var triggerReload by remember { mutableStateOf(0) }

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

            val goles = db.collection("goleadores")
                .whereEqualTo("partidoUid", partidoUid)
                .get().await()
                .documents.mapNotNull { it.toObject(GoleadorFirebase::class.java)?.copy(uid = it.id) }

            val jugadorUids = goles.map { it.jugadorUid } + goles.mapNotNull { it.asistenciaJugadorUid }
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
                .sortedBy { it.minuto ?: Int.MAX_VALUE }
                .map { gol ->
                    GolEvento(
                        equipoUid = gol.equipoUid,
                        jugador = if (!gol.jugadorUid.isNullOrBlank()) {
                            nombresMap[gol.jugadorUid] ?: gol.jugadorNombreManual ?: "Desconocido"
                        } else {
                            gol.jugadorNombreManual ?: "Desconocido"
                        },
                        minuto = gol.minuto,
                        asistente = when {
                            !gol.asistenciaJugadorUid.isNullOrBlank() -> nombresMap[gol.asistenciaJugadorUid] ?: gol.asistenciaNombreManual ?: "Desconocido"
                            !gol.asistenciaNombreManual.isNullOrBlank() -> gol.asistenciaNombreManual
                            else -> null
                        }
                    )
                }

            isLoading = false
        }
    }

    // UI
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
                Icon(Icons.Default.Refresh, contentDescription = "Recargar goles", tint = TorneoYaPalette.blue)
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
                "Sin eventos registrados",
                style = MaterialTheme.typography.bodyMedium,
                color = TorneoYaPalette.mutedText,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            return@Column
        }

        eventos.forEach { evento ->
            val isEquipoA = evento.equipoUid == equipoAUid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = if (isEquipoA) Arrangement.Start else Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = if (isEquipoA)
                                Brush.horizontalGradient(listOf(TorneoYaPalette.blue.copy(alpha = 0.18f), Color.Transparent))
                            else
                                Brush.horizontalGradient(listOf(Color.Transparent, TorneoYaPalette.violet.copy(alpha = 0.18f))),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 2.dp,
                            brush = if (isEquipoA)
                                Brush.horizontalGradient(listOf(TorneoYaPalette.blue, TorneoYaPalette.violet))
                            else
                                Brush.horizontalGradient(listOf(TorneoYaPalette.violet, TorneoYaPalette.blue)),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(vertical = 11.dp, horizontal = 16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⚽",
                                fontSize = 20.sp,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                text = evento.jugador,
                                fontWeight = FontWeight.Bold,
                                color = if (isEquipoA) TorneoYaPalette.blue else TorneoYaPalette.violet,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            evento.minuto?.let {
                                Text(
                                    text = "${it}'",
                                    color = TorneoYaPalette.mutedText,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                            }
                        }
                        if (!evento.asistente.isNullOrBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text("🅰️", fontSize = 16.sp, modifier = Modifier.padding(end = 4.dp))
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
