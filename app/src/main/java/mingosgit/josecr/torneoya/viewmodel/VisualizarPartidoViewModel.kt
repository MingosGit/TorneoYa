package mingosgit.josecr.torneoya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.repository.PartidoRepository
import mingosgit.josecr.torneoya.repository.EquipoRepository

data class VisualizarPartidoUiState(
    val nombreEquipoA: String = "",
    val nombreEquipoB: String = "",
    val jugadoresEquipoA: List<String> = emptyList(),
    val jugadoresEquipoB: List<String> = emptyList()
)

class VisualizarPartidoViewModel(
    private val partidoId: Long,
    private val partidoRepository: PartidoRepository,
    private val equipoRepository: EquipoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VisualizarPartidoUiState())
    val uiState: StateFlow<VisualizarPartidoUiState> = _uiState

    fun cargarDatos() {
        viewModelScope.launch {
            val partido = partidoRepository.getPartidoById(partidoId)
            if (partido != null) {
                val equipoAId = partido.equipoAId
                val equipoBId = partido.equipoBId

                // DEBUG: imprime IDs
                println("CARGAR DATOS partidoId=$partidoId equipoAId=$equipoAId equipoBId=$equipoBId")

                val nombreEquipoA = equipoRepository.getById(equipoAId)?.nombre ?: "Equipo A"
                val nombreEquipoB = equipoRepository.getById(equipoBId)?.nombre ?: "Equipo B"
                val jugadoresA = equipoRepository.getNombresJugadoresEquipoEnPartido(partidoId, equipoAId)
                val jugadoresB = equipoRepository.getNombresJugadoresEquipoEnPartido(partidoId, equipoBId)

                // DEBUG: imprime jugadores
                println("Jugadores A: $jugadoresA")
                println("Jugadores B: $jugadoresB")

                _uiState.value = VisualizarPartidoUiState(
                    nombreEquipoA = nombreEquipoA,
                    nombreEquipoB = nombreEquipoB,
                    jugadoresEquipoA = jugadoresA,
                    jugadoresEquipoB = jugadoresB
                )
            }
        }
    }

    init {
        cargarDatos()
    }
}

class VisualizarPartidoViewModelFactory(
    private val partidoId: Long,
    private val partidoRepository: PartidoRepository,
    private val equipoRepository: EquipoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VisualizarPartidoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VisualizarPartidoViewModel(
                partidoId = partidoId,
                partidoRepository = partidoRepository,
                equipoRepository = equipoRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
