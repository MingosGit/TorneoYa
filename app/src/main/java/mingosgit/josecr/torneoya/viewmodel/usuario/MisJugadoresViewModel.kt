package mingosgit.josecr.torneoya.viewmodel.usuario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.entities.JugadorEntity
import mingosgit.josecr.torneoya.repository.JugadorRepository

class MisJugadoresViewModel(
    private val jugadorRepository: JugadorRepository
) : ViewModel() {

    private val _jugadores = MutableStateFlow<List<JugadorEntity>>(emptyList())
    val jugadores: StateFlow<List<JugadorEntity>> = _jugadores

    init {
        cargarJugadores()
    }

    fun cargarJugadores() {
        viewModelScope.launch {
            _jugadores.value = jugadorRepository.getAll()
        }
    }
}
