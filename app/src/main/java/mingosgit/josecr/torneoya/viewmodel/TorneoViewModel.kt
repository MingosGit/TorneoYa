package mingosgit.josecr.torneoya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.entities.TorneoEntity
import mingosgit.josecr.torneoya.repository.TorneosRepository

class TorneoViewModel(
    private val torneosRepo: TorneosRepository
) : ViewModel() {

    private val _torneo = MutableStateFlow<TorneoEntity?>(null)
    val torneo: StateFlow<TorneoEntity?> = _torneo

    fun cargarTorneo(id: Long) {
        viewModelScope.launch {
            _torneo.value = torneosRepo.getTorneoById(id)
        }
    }

    fun guardarTorneo(torneo: TorneoEntity, onFinish: (Long) -> Unit) {
        viewModelScope.launch {
            val id = if (torneo.id == 0L)
                torneosRepo.insertTorneo(torneo)
            else {
                torneosRepo.updateTorneo(torneo)
                torneo.id
            }
            onFinish(id)
        }
    }

    fun eliminarTorneo(torneo: TorneoEntity, onFinish: () -> Unit) {
        viewModelScope.launch {
            torneosRepo.deleteTorneo(torneo)
            onFinish()
        }
    }
}
