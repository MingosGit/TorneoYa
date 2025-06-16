package mingosgit.josecr.torneoya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.entities.PartidoEntity
import mingosgit.josecr.torneoya.repository.PartidoRepository

class EditPartidoViewModel(
    private val partidoRepository: PartidoRepository,
    private val partidoId: Long
) : ViewModel() {

    private val _partido = MutableStateFlow<PartidoEntity?>(null)
    val partido: StateFlow<PartidoEntity?> get() = _partido

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> get() = _loading

    private val _eliminado = MutableStateFlow(false)
    val eliminado: StateFlow<Boolean> get() = _eliminado

    private val _guardado = MutableStateFlow(false)
    val guardado: StateFlow<Boolean> get() = _guardado

    init {
        cargarPartido()
    }

    fun cargarPartido() {
        viewModelScope.launch {
            _loading.value = true
            _partido.value = partidoRepository.getPartidoById(partidoId)
            _loading.value = false
        }
    }

    fun actualizarPartido(
        equipoA: String,
        equipoB: String,
        fecha: String,
        horaInicio: String,
        numeroPartes: Int,
        tiempoPorParte: Int
    ) {
        val p = _partido.value ?: return
        val nuevo = p.copy(
            equipoA = equipoA,
            equipoB = equipoB,
            fecha = fecha,
            horaInicio = horaInicio,
            numeroPartes = numeroPartes,
            tiempoPorParte = tiempoPorParte
        )
        viewModelScope.launch {
            partidoRepository.updatePartido(nuevo)
            _partido.value = nuevo
            _guardado.value = true
        }
    }

    fun eliminarPartido() {
        val p = _partido.value ?: return
        viewModelScope.launch {
            partidoRepository.deletePartido(p)
            _eliminado.value = true
        }
    }
}
