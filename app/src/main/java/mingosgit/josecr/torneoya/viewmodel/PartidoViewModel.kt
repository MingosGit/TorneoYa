package mingosgit.josecr.torneoya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.entities.PartidoEntity
import mingosgit.josecr.torneoya.repository.PartidosRepository
import mingosgit.josecr.torneoya.repository.EquiposRepository
import mingosgit.josecr.torneoya.repository.IntegrantesRepository

data class PartidoCompletoUI(
    val partido: PartidoEntity,
    val nombreEquipoLocal: String,
    val nombreEquipoVisitante: String,
    val integrantesLocal: List<String>,
    val integrantesVisitante: List<String>
)

class PartidoViewModel(
    private val partidosRepo: PartidosRepository,
    private val equiposRepo: EquiposRepository,
    private val integrantesRepo: IntegrantesRepository
) : ViewModel() {

    private val _ui = MutableStateFlow<PartidoCompletoUI?>(null)
    val ui: StateFlow<PartidoCompletoUI?> = _ui

    fun cargarPartidoCompleto(partidoId: Long) {
        viewModelScope.launch {
            val partido = partidosRepo.getPartidoById(partidoId) ?: return@launch

            val equipoLocal = equiposRepo.getEquipoById(partido.equipoLocalId)
            val equipoVisitante = equiposRepo.getEquipoById(partido.equipoVisitanteId)

            val integrantesLocal = equipoLocal?.let { integrantesRepo.getIntegrantesByEquipoId(it.id) } ?: emptyList()
            val integrantesVisitante = equipoVisitante?.let { integrantesRepo.getIntegrantesByEquipoId(it.id) } ?: emptyList()

            _ui.value = PartidoCompletoUI(
                partido = partido,
                nombreEquipoLocal = equipoLocal?.nombre ?: "Equipo local",
                nombreEquipoVisitante = equipoVisitante?.nombre ?: "Equipo visitante",
                integrantesLocal = integrantesLocal.map { it.nombre },
                integrantesVisitante = integrantesVisitante.map { it.nombre }
            )
        }
    }

    fun eliminarPartido(partido: PartidoEntity, onFinish: () -> Unit) {
        viewModelScope.launch {
            partidosRepo.deletePartido(partido)
            onFinish()
        }
    }

    // Puedes añadir aquí el método guardarPartido si te hace falta
    fun guardarPartido(partido: PartidoEntity, onFinish: () -> Unit) {
        viewModelScope.launch {
            partidosRepo.insertPartido(partido)
            onFinish()
        }
    }
}
