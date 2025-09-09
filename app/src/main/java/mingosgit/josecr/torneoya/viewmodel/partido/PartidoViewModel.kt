package mingosgit.josecr.torneoya.viewmodel.partido

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import mingosgit.josecr.torneoya.data.entities.PartidoEntity
import mingosgit.josecr.torneoya.data.entities.PartidoEquipoJugadorEntity
import mingosgit.josecr.torneoya.repository.PartidoRepository
import mingosgit.josecr.torneoya.repository.EquipoRepository

data class PartidoConNombres(
    val id: Long,
    val nombreEquipoA: String,
    val nombreEquipoB: String,
    val fecha: String,
    val horaInicio: String,
    val horaFin: String
)

// ViewModel de gestión de partidos (lista, altas, bajas y utilidades de presentación)
class PartidoViewModel(private val repository: PartidoRepository) : ViewModel() {

    // Estado: lista cruda de entidades Partido
    private val _partidos = MutableStateFlow<List<PartidoEntity>>(emptyList())
    val partidos: StateFlow<List<PartidoEntity>> = _partidos

    // Estado: lista enriquecida con nombres de equipos y hora fin calculada
    private val _partidosConNombres = MutableStateFlow<List<PartidoConNombres>>(emptyList())
    val partidosConNombres: StateFlow<List<PartidoConNombres>> = _partidosConNombres

    // cargarPartidos: obtiene todos los partidos (sin enriquecer) y actualiza estado
    fun cargarPartidos() {
        viewModelScope.launch {
            _partidos.value = repository.getAllPartidos()
        }
    }

    // cargarPartidosConNombres: compone DTO con nombres de equipos y hora fin calculada
    fun cargarPartidosConNombres(equipoRepository: EquipoRepository) {
        viewModelScope.launch {
            val partidos = repository.getAllPartidos()
            val partidosNombres = partidos.map { partido ->
                val equipoA = equipoRepository.getById(partido.equipoAId)
                val equipoB = equipoRepository.getById(partido.equipoBId)
                PartidoConNombres(
                    id = partido.id,
                    nombreEquipoA = equipoA?.nombre ?: "Equipo A",
                    nombreEquipoB = equipoB?.nombre ?: "Equipo B",
                    fecha = partido.fecha,
                    horaInicio = partido.horaInicio,
                    horaFin = calcularHoraFin(
                        partido.horaInicio,
                        partido.numeroPartes,
                        partido.tiempoPorParte,
                        partido.tiempoDescanso
                    )
                )
            }
            _partidosConNombres.value = partidosNombres
        }
    }

    // calcularHoraFin: calcula la hora de fin sumando partes y descansos a la hora de inicio
    private fun calcularHoraFin(horaInicio: String, numeroPartes: Int, tiempoPorParte: Int, tiempoDescanso: Int): String {
        return try {
            val partesTotal = (numeroPartes * tiempoPorParte) + ((numeroPartes - 1) * tiempoDescanso)
            val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
            val inicio = java.time.LocalTime.parse(horaInicio, formatter)
            val fin = inicio.plusMinutes(partesTotal.toLong())
            fin.format(formatter)
        } catch (e: Exception) {
            ""
        }
    }

    // agregarPartido: inserta un partido y refresca la lista enriquecida
    fun agregarPartido(partido: PartidoEntity, equipoRepository: EquipoRepository) {
        viewModelScope.launch {
            repository.insertPartido(partido)
            cargarPartidosConNombres(equipoRepository)
        }
    }

    // asignarJugadorAPartido: crea relación partido-equipo-jugador
    fun asignarJugadorAPartido(partidoId: Long, equipoId: Long, jugadorId: Long) {
        viewModelScope.launch {
            val rel = PartidoEquipoJugadorEntity(partidoId, equipoId, jugadorId)
            repository.asignarJugadorAPartido(rel)
        }
    }

    // eliminarJugadorDePartido: elimina la relación del jugador con ambos equipos del partido
    fun eliminarJugadorDePartido(partidoId: Long, equipoAId: Long, equipoBId: Long, jugadorId: Long) {
        viewModelScope.launch {
            val relA = PartidoEquipoJugadorEntity(partidoId, equipoAId, jugadorId)
            val relB = PartidoEquipoJugadorEntity(partidoId, equipoBId, jugadorId)
            repository.eliminarJugadorDePartido(relA)
            repository.eliminarJugadorDePartido(relB)
        }
    }

    // eliminarPartido: borra por id y refresca la lista enriquecida
    fun eliminarPartido(partidoId: Long, equipoRepository: EquipoRepository) {
        viewModelScope.launch {
            repository.deletePartidoById(partidoId)
            cargarPartidosConNombres(equipoRepository)
        }
    }

    // duplicarPartido: clona un partido (id a 0L) y refresca la lista enriquecida
    fun duplicarPartido(partidoId: Long, equipoRepository: EquipoRepository) {
        viewModelScope.launch {
            val partido = repository.getPartidoById(partidoId)
            partido?.let {
                val nuevoPartido = it.copy(id = 0L)
                repository.insertPartido(nuevoPartido)
            }
            cargarPartidosConNombres(equipoRepository)
        }
    }
}
