package mingosgit.josecr.torneoya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.entities.PartidoEntity
import mingosgit.josecr.torneoya.data.entities.TorneoEntity
import mingosgit.josecr.torneoya.repository.PartidosRepository
import mingosgit.josecr.torneoya.repository.TorneosRepository
import mingosgit.josecr.torneoya.repository.EquiposRepository

data class PartidoUI(
    val id: Long,
    val nombreEquipoLocal: String,
    val nombreEquipoVisitante: String,
    val fecha: Long,
    val torneoId: Long?
)

class HomeViewModel(
    private val torneosRepo: TorneosRepository,
    private val partidosRepo: PartidosRepository,
    private val equiposRepo: EquiposRepository
) : ViewModel() {

    private val _torneos = MutableStateFlow<List<TorneoEntity>>(emptyList())
    val torneos: StateFlow<List<TorneoEntity>> = _torneos

    private val _partidosUI = MutableStateFlow<List<PartidoUI>>(emptyList())
    val partidosUI: StateFlow<List<PartidoUI>> = _partidosUI

    fun cargarDatos() {
        viewModelScope.launch {
            _torneos.value = torneosRepo.getAllTorneos()
            val partidos = partidosRepo.getAllPartidos()
            val equipos = equiposRepo.getAllEquipos()
            _partidosUI.value = partidos.map { partido ->
                PartidoUI(
                    id = partido.id,
                    nombreEquipoLocal = equipos.find { it.id == partido.equipoLocalId }?.nombre ?: "Equipo A",
                    nombreEquipoVisitante = equipos.find { it.id == partido.equipoVisitanteId }?.nombre ?: "Equipo B",
                    fecha = partido.fecha,
                    torneoId = partido.torneoId
                )
            }
        }
    }
}
