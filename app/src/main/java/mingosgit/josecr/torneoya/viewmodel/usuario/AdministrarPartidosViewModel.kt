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

// ViewModel para gestionar partidos, goles y eventos desde UI.
class AdministrarPartidosViewModel(
    private val partidoRepository: PartidoRepository,   // Acceso a datos de partidos
    private val goleadorRepository: GoleadorRepository, // Acceso a datos de goleadores
    private val eventoRepository: EventoRepository      // Acceso a eventos cronológicos del partido
) : ViewModel() {

    // Estado: lista de partidos visible para la UI
    private val _partidos = MutableStateFlow<List<PartidoEntity>>(emptyList())
    val partidos: StateFlow<List<PartidoEntity>> = _partidos

    // Estado: texto de búsqueda para filtrar partidos
    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda

    // Estado: lista de goleadores del partido seleccionado
    private val _goleadores = MutableStateFlow<List<GoleadorEntity>>(emptyList())
    val goleadores: StateFlow<List<GoleadorEntity>> = _goleadores

    // Actualiza la cadena de búsqueda y aplica el filtro.
    fun setBusqueda(query: String) {
        _busqueda.value = query
        filtrarPartidos()
    }

    // Carga todos los partidos desde el repositorio.
    fun cargarPartidos() {
        viewModelScope.launch {
            _partidos.value = partidoRepository.getAllPartidos()
        }
    }

    // Aplica el filtro actual de búsqueda sobre los partidos.
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

    // Carga los goleadores asociados a un partido concreto.
    fun cargarGoleadores(partidoId: Long) {
        viewModelScope.launch {
            _goleadores.value = goleadorRepository.getGolesPorPartido(partidoId)
        }
    }

    // Inserta un gol y registra su evento; después refresca marcador y lista de goleadores.
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

    // Elimina un gol y actualiza marcador y goleadores del partido.
    fun borrarGol(gol: GoleadorEntity) {
        viewModelScope.launch {
            goleadorRepository.borrarGol(gol)
            actualizarGolesPartido(gol.partidoId)
            cargarGoleadores(gol.partidoId)
        }
    }

    // Cuenta goles de un equipo en un partido (consulta y devuelve el tamaño de la lista).
    private suspend fun contarGoles(partidoId: Long, equipoId: Long): Int {
        return goleadorRepository.getGolesPorEquipoEnPartido(partidoId, equipoId).size
    }

    // Recalcula y guarda el marcador del partido a partir de los goles registrados.
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

    // Actualiza manualmente el marcador de un partido y refresca la lista.
    fun actualizarGoles(partido: PartidoEntity, golesA: Int, golesB: Int) {
        viewModelScope.launch {
            partidoRepository.actualizarGoles(partido.id, golesA, golesB)
            cargarPartidos()
        }
    }

    // Factory para inyectar repositorios al crear el ViewModel.
    class Factory(
        private val partidoRepository: PartidoRepository,
        private val goleadorRepository: GoleadorRepository,
        private val eventoRepository: EventoRepository
    ) : ViewModelProvider.Factory {
        // Crea una instancia de AdministrarPartidosViewModel si el tipo coincide.
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AdministrarPartidosViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AdministrarPartidosViewModel(
                    partidoRepository,
                    goleadorRepository,
                    eventoRepository
                ) as T
            }
            // Error si se pide un ViewModel de tipo desconocido.
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
