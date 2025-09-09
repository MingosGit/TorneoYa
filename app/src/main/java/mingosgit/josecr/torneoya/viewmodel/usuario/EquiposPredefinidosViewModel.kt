package mingosgit.josecr.torneoya.viewmodel.equipopredefinido

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.dao.EquipoPredefinidoConJugadores
import mingosgit.josecr.torneoya.data.entities.EquipoPredefinidoEntity
import mingosgit.josecr.torneoya.repository.EquipoPredefinidoRepository

// ViewModel para gestionar equipos predefinidos y sus jugadores
class EquiposPredefinidosViewModel(
    private val repository: EquipoPredefinidoRepository
) : ViewModel() {

    // Estado: lista de equipos con sus jugadores
    private val _equipos = MutableStateFlow<List<EquipoPredefinidoConJugadores>>(emptyList())
    val equipos: StateFlow<List<EquipoPredefinidoConJugadores>> = _equipos

    // Al iniciar carga todos los equipos
    init {
        cargarEquipos()
    }

    // Obtiene todos los equipos con sus jugadores desde el repositorio
    fun cargarEquipos() {
        viewModelScope.launch {
            _equipos.value = repository.getAllConJugadores()
        }
    }

    // Vuelve a cargar equipos (alias de cargarEquipos)
    fun recargarEquipos() {
        cargarEquipos()
    }

    // Elimina un equipo y refresca la lista
    fun eliminarEquipo(equipo: EquipoPredefinidoEntity) {
        viewModelScope.launch {
            repository.deleteEquipo(equipo)
            cargarEquipos()
        }
    }

}
