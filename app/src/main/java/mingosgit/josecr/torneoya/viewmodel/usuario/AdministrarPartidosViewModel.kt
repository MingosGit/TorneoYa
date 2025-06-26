package mingosgit.josecr.torneoya.viewmodel.usuario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.entities.PartidoEntity
import mingosgit.josecr.torneoya.data.entities.GoleadorEntity
import mingosgit.josecr.torneoya.repository.PartidoRepository
import mingosgit.josecr.torneoya.repository.GoleadorRepository
import mingosgit.josecr.torneoya.repository.EventoRepository
import mingosgit.josecr.torneoya.data.entities.EventoEntity

class AdministrarPartidosViewModel(
    private val partidoRepository: PartidoRepository,
    private val goleadorRepository: GoleadorRepository,
    private val eventoRepository: EventoRepository
) : ViewModel() {

    private val _partidos = MutableStateFlow<List<PartidoEntity>>(emptyList())
    val partidos: StateFlow<List<PartidoEntity>> = _partidos

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda

    private val _goleadores = MutableStateFlow<List<GoleadorEntity>>(emptyList())
    val goleadores: StateFlow<List<GoleadorEntity>> = _goleadores

    fun setBusqueda(query: String) {
        _busqueda.value = query
        filtrarPartidos()
    }

    fun cargarPartidos() {
        viewModelScope.launch {
            _partidos.value = partidoRepository.getAllPartidos()
        }
    }

    private fun filtrarPartidos() {
        viewModelScope.launch {
            val query = _busqueda.value.trim().lowercase()
            val all = partidoRepository.getAllPartidos()
            if (query.isBlank()) {
                _partidos.value = all
            } else {
                _partidos.value = all.filter {
                    it.fecha.contains(query, true) ||
                            it.id.toString() == query
                }
            }
        }
    }

    fun cargarGoleadores(partidoId: Long) {
        viewModelScope.launch {
            _goleadores.value = goleadorRepository.getGolesPorPartido(partidoId)
        }
    }

    fun agregarGol(
        partidoId: Long,
        equipoId: Long,
        jugadorId: Long,
        minuto: Int?,
        asistenciaJugadorId: Long?
    ) {
        viewModelScope.launch {
            goleadorRepository.insertarGol(
                partidoId,
                equipoId,
                jugadorId,
                minuto,
                asistenciaJugadorId
            )
            // Nuevo: agregar evento
            val fechaHora = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            eventoRepository.agregarEvento(
                EventoEntity(
                    partidoId = partidoId,
                    tipo = "GOL",
                    minuto = minuto,
                    equipoId = equipoId,
                    jugadorId = jugadorId,
                    asistenteId = asistenciaJugadorId,
                    fechaHora = fechaHora
                )
            )
            actualizarGolesPartido(partidoId)
            cargarGoleadores(partidoId)
        }
    }

    fun borrarGol(gol: GoleadorEntity) {
        viewModelScope.launch {
            goleadorRepository.borrarGol(gol)
            actualizarGolesPartido(gol.partidoId)
            cargarGoleadores(gol.partidoId)
        }
    }

    private suspend fun contarGoles(partidoId: Long, equipoId: Long): Int {
        return goleadorRepository.getGolesPorEquipoEnPartido(partidoId, equipoId).size
    }

    private fun actualizarGolesPartido(partidoId: Long) {
        viewModelScope.launch {
            val partido = partidoRepository.getPartidoById(partidoId)
            if (partido != null) {
                val golesA = contarGoles(partidoId, partido.equipoAId)
                val golesB = contarGoles(partidoId, partido.equipoBId)
                partidoRepository.actualizarGoles(partidoId, golesA, golesB)
                cargarPartidos()
            }
        }
    }

    fun actualizarGoles(partido: PartidoEntity, golesA: Int, golesB: Int) {
        viewModelScope.launch {
            partidoRepository.actualizarGoles(partido.id, golesA, golesB)
            cargarPartidos()
        }
    }

    class Factory(
        private val partidoRepository: PartidoRepository,
        private val goleadorRepository: GoleadorRepository,
        private val eventoRepository: EventoRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AdministrarPartidosViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AdministrarPartidosViewModel(
                    partidoRepository,
                    goleadorRepository,
                    eventoRepository
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
