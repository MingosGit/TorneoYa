package mingosgit.josecr.torneoya.viewmodel.equipopredefinido

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.dao.EquipoPredefinidoConJugadores
import mingosgit.josecr.torneoya.data.entities.EquipoPredefinidoEntity
import mingosgit.josecr.torneoya.repository.EquipoPredefinidoRepository

class EquiposPredefinidosViewModel(
    private val repository: EquipoPredefinidoRepository
) : ViewModel() {

    private val _equipos = MutableStateFlow<List<EquipoPredefinidoConJugadores>>(emptyList())
    val equipos: StateFlow<List<EquipoPredefinidoConJugadores>> = _equipos

    init {
        cargarEquipos()
    }

    fun cargarEquipos() {
        viewModelScope.launch {
            _equipos.value = repository.getAllConJugadores()
        }
    }

    fun agregarEquipo(nombre: String) {
        viewModelScope.launch {
            repository.insertEquipo(EquipoPredefinidoEntity(nombre = nombre))
            cargarEquipos()
        }
    }

    fun eliminarEquipo(equipo: EquipoPredefinidoEntity) {
        viewModelScope.launch {
            repository.deleteEquipo(equipo)
            cargarEquipos()
        }
    }

    fun agregarJugadorAEquipo(equipoId: Long, jugadorId: Long) {
        viewModelScope.launch {
            repository.agregarJugador(equipoId, jugadorId)
            cargarEquipos()
        }
    }

    fun quitarJugadorDeEquipo(equipoId: Long, jugadorId: Long) {
        viewModelScope.launch {
            repository.quitarJugador(equipoId, jugadorId)
            cargarEquipos()
        }
    }
}
