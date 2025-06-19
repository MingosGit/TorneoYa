package mingosgit.josecr.torneoya.viewmodel.partido

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import mingosgit.josecr.torneoya.data.entities.JugadorEntity
import mingosgit.josecr.torneoya.repository.JugadorRepository

class JugadorViewModel(private val repository: JugadorRepository) : ViewModel() {
    private val _jugadores = MutableStateFlow<List<JugadorEntity>>(emptyList())
    val jugadores: StateFlow<List<JugadorEntity>> = _jugadores

    fun cargarJugadores() {
        viewModelScope.launch {
            _jugadores.value = repository.getAll()
        }
    }

    fun agregarJugador(jugador: JugadorEntity) {
        viewModelScope.launch {
            repository.insertJugador(jugador)
            cargarJugadores()
        }
    }

    fun borrarJugador(jugador: JugadorEntity) {
        viewModelScope.launch {
            repository.deleteJugador(jugador)
            cargarJugadores()
        }
    }
}
