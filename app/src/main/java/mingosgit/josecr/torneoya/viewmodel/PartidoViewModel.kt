package mingosgit.josecr.torneoya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import mingosgit.josecr.torneoya.data.entities.PartidoEntity
import mingosgit.josecr.torneoya.data.entities.PartidoEquipoJugadorEntity
import mingosgit.josecr.torneoya.repository.PartidoRepository

class PartidoViewModel(private val repository: PartidoRepository) : ViewModel() {
    private val _partidos = MutableStateFlow<List<PartidoEntity>>(emptyList())
    val partidos: StateFlow<List<PartidoEntity>> = _partidos

    fun cargarPartidos() {
        viewModelScope.launch {
            _partidos.value = repository.getAllPartidos()
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
