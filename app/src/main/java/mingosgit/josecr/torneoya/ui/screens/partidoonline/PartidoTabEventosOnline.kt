package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mingosgit.josecr.torneoya.data.firebase.GoleadorFirebase
import mingosgit.josecr.torneoya.data.firebase.EquipoFirebase

@Composable
fun PartidoTabEventosOnline(
    partidoUid: String,
    uiState: mingosgit.josecr.torneoya.viewmodel.partidoonline.VisualizarPartidoOnlineUiState
) {
    val scope = rememberCoroutineScope()
    var eventos by remember { mutableStateOf<List<GolEvento>>(emptyList()) }
    var equipoAUid by remember { mutableStateOf<String?>(null) }
    var equipoBUid by remember { mutableStateOf<String?>(null) }
    var nombreA by remember { mutableStateOf(uiState.nombreEquipoA) }
    var nombreB by remember { mutableStateOf(uiState.nombreEquipoB) }

    LaunchedEffect(partidoUid) {
        scope.launch {
            val db = FirebaseFirestore.getInstance()
            // Obtener partido (solo los uids de equipos)
            val snap = db.collection("partidos").document(partidoUid).get().await()
            equipoAUid = snap.getString("equipoAId")
            equipoBUid = snap.getString("equipoBId")

            // Obtener nombres equipos (por si cambian)
            equipoAUid?.let {
                val equipoA = db.collection("equipos").document(it).get().await().toObject(EquipoFirebase::class.java)
                if (equipoA?.nombre?.isNotBlank() == true) nombreA = equipoA.nombre
            }
            equipoBUid?.let {
                val equipoB = db.collection("equipos").document(it).get().await().toObject(EquipoFirebase::class.java)
                if (equipoB?.nombre?.isNotBlank() == true) nombreB = equipoB.nombre
            }

            // Obtener todos los goles de ese partido
            val goles = db.collection("goleadores")
                .whereEqualTo("partidoUid", partidoUid)
                .get().await()
                .documents.mapNotNull { it.toObject(GoleadorFirebase::class.java)?.copy(uid = it.id) }

            // Mapear jugadorUid -> nombre
            val jugadorUids = goles.map { it.jugadorUid } + goles.mapNotNull { it.asistenciaJugadorUid }
            val jugadoresDocs = jugadorUids.distinct()
                .filter { it.isNotBlank() }
                .mapNotNull { uid ->
                    val jugSnap = db.collection("jugadores").document(uid).get().await()
                    if (jugSnap.exists()) uid to (jugSnap.getString("nombre") ?: "") else null
                }
            // Usuarios fallback (por si no existe en jugadores)
            val faltantes = jugadorUids.distinct().filter { it.isNotBlank() && jugadoresDocs.none { pair -> pair.first == it } }
            val usuariosDocs = faltantes.mapNotNull { uid ->
                val userSnap = db.collection("usuarios").document(uid).get().await()
                if (userSnap.exists()) uid to (userSnap.getString("nombreUsuario") ?: "") else null
            }
            val nombresMap = (jugadoresDocs + usuariosDocs).toMap()

            // Construir lista de eventos
            eventos = goles
                .sortedBy { it.minuto ?: Int.MAX_VALUE }
                .map { gol ->
                    GolEvento(
                        equipoUid = gol.equipoUid,
                        jugador = nombresMap[gol.jugadorUid] ?: "Desconocido",
                        minuto = gol.minuto,
                        asistente = gol.asistenciaJugadorUid?.let { nombresMap[it] },
                    )
                }
        }
    }

    // UI
    Column(Modifier.fillMaxWidth().padding(12.dp)) {
        if (eventos.isEmpty()) {
            Text("Sin eventos registrados", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
            return@Column
        }

        eventos.forEach { evento ->
            val isEquipoA = evento.equipoUid == equipoAUid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalAlignment = if (isEquipoA) Alignment.Start else Alignment.End
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚽", fontSize = MaterialTheme.typography.titleLarge.fontSize)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        evento.jugador + (evento.minuto?.let { "  ${it}'" } ?: ""),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                if (!evento.asistente.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🅰️", fontSize = MaterialTheme.typography.titleMedium.fontSize)
                        Spacer(Modifier.width(4.dp))
                        Text(evento.asistente, style = MaterialTheme.typography.bodyMedium)
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
