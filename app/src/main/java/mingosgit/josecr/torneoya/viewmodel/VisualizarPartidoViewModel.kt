package mingosgit.josecr.torneoya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.entities.PartidoEntity
import mingosgit.josecr.torneoya.repository.PartidoRepository
import mingosgit.josecr.torneoya.repository.EquipoRepository

class VisualizarPartidoViewModel(
    private val partidoId: Long,
    private val partidoRepository: PartidoRepository,
    private val equipoRepository: EquipoRepository
) : ViewModel() {

    private val _partido = MutableStateFlow<PartidoEntity?>(null)
    val partido: StateFlow<PartidoEntity?> = _partido

    private val _nombreEquipoA = MutableStateFlow("")
    val nombreEquipoA: StateFlow<String> = _nombreEquipoA

    private val _nombreEquipoB = MutableStateFlow("")
    val nombreEquipoB: StateFlow<String> = _nombreEquipoB

    private val _jugadoresEquipoA = MutableStateFlow<List<String>>(emptyList())
    val jugadoresEquipoA: StateFlow<List<String>> = _jugadoresEquipoA

    private val _jugadoresEquipoB = MutableStateFlow<List<String>>(emptyList())
    val jugadoresEquipoB: StateFlow<List<String>> = _jugadoresEquipoB

    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        viewModelScope.launch {
            _cargando.value = true
            val partidoEntity = partidoRepository.getPartidoById(partidoId)
            _partido.value = partidoEntity

            if (partidoEntity != null) {
                val equipoA = equipoRepository.getById(partidoEntity.equipoAId)
                val equipoB = equipoRepository.getById(partidoEntity.equipoBId)
                _nombreEquipoA.value = equipoA?.nombre ?: "Equipo A"
                _nombreEquipoB.value = equipoB?.nombre ?: "Equipo B"

                // *** IMPORTANTE: getJugadoresDeEquipoEnPartido debe devolver List<JugadorEntity>
                val jugadoresA = partidoRepository.getJugadoresDeEquipoEnPartido(partidoId, partidoEntity.equipoAId)
                val jugadoresB = partidoRepository.getJugadoresDeEquipoEnPartido(partidoId, partidoEntity.equipoBId)

                _jugadoresEquipoA.value = jugadoresA.map { it.nombre }
                _jugadoresEquipoB.value = jugadoresB.map { it.nombre }
            }
            _cargando.value = false
        }
    }
}

class VisualizarPartidoViewModelFactory(
    private val partidoId: Long,
    private val partidoRepository: PartidoRepository,
    private val equipoRepository: EquipoRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VisualizarPartidoViewModel::class.java)) {
            return VisualizarPartidoViewModel(partidoId, partidoRepository, equipoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
