package mingosgit.josecr.torneoya.ui.screens.partido.visualizarpartidoscreen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.entities.EventoEntity
import mingosgit.josecr.torneoya.repository.EventoRepository
import mingosgit.josecr.torneoya.repository.JugadorRepository
import mingosgit.josecr.torneoya.repository.EquipoRepository

@Composable
fun PartidoTabEventos(
    partidoId: Long,
    eventoRepository: EventoRepository,
    jugadorRepository: JugadorRepository,
    equipoRepository: EquipoRepository
) {
    var eventos by remember { mutableStateOf<List<EventoEntity>>(emptyList()) }
    var jugadores by remember { mutableStateOf<Map<Long, String>>(emptyMap()) }
    var equipos by remember { mutableStateOf<Map<Long, String>>(emptyMap()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(partidoId) {
        eventos = eventoRepository.getEventosPorPartido(partidoId)
        // Obtener nombres de jugadores y equipos involucrados
        val jugadorIds = eventos.map { it.jugadorId } + eventos.mapNotNull { it.asistenteId }
        val equipoIds = eventos.map { it.equipoId }
        val jugadoresMap = jugadorIds.distinct().associateWith { id -> jugadorRepository.getById(id)?.nombre ?: "?" }
        val equiposMap = equipoIds.distinct().associateWith { id -> equipoRepository.getById(id)?.nombre ?: "?" }
        jugadores = jugadoresMap
        equipos = equiposMap
    }

    if (eventos.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Sin eventos",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            eventos.forEach { evento ->
                if (evento.tipo == "GOL") {
                    val nombreJugador = jugadores[evento.jugadorId] ?: "?"
                    val nombreEquipo = equipos[evento.equipoId] ?: "?"
                    val asistente = evento.asistenteId?.let { jugadores[it] } ?: ""
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚽ ${evento.minuto?.toString() ?: "-"}' $nombreJugador (${nombreEquipo})" +
                                    if (asistente.isNotEmpty()) " | Asist: $asistente" else "",
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = evento.fechaHora.take(16),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Divider()
                }
            }
        }
    }
}
