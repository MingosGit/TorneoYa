package mingosgit.josecr.torneoya.viewmodel

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
    val fecha: String
)

class PartidoViewModel(private val repository: PartidoRepository) : ViewModel() {
    private val _partidos = MutableStateFlow<List<PartidoEntity>>(emptyList())
    val partidos: StateFlow<List<PartidoEntity>> = _partidos

    private val _partidosConNombres = MutableStateFlow<List<PartidoConNombres>>(emptyList())
    val partidosConNombres: StateFlow<List<PartidoConNombres>> = _partidosConNombres

    fun cargarPartidos() {
        viewModelScope.launch {
            _partidos.value = repository.getAllPartidos()
        }
    }

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
                    fecha = partido.fecha
                )
            }
            _partidosConNombres.value = partidosNombres
        }
    }

    fun agregarPartido(partido: PartidoEntity) {
        viewModelScope.launch {
            repository.insertPartido(partido)
            cargarPartidos()
        }
    }

    fun asignarJugadorAPartido(partidoId: Long, equipoId: Long, jugadorId: Long) {
        viewModelScope.launch {
            val rel = PartidoEquipoJugadorEntity(partidoId, equipoId, jugadorId)
            repository.asignarJugadorAPartido(rel)
        }
    }

    fun eliminarJugadorDePartido(partidoId: Long, equipoAId: Long, equipoBId: Long, jugadorId: Long) {
        viewModelScope.launch {
            val relA = PartidoEquipoJugadorEntity(partidoId, equipoAId, jugadorId)
            val relB = PartidoEquipoJugadorEntity(partidoId, equipoBId, jugadorId)
            repository.eliminarJugadorDePartido(relA)
            repository.eliminarJugadorDePartido(relB)
        }
    }
}
