package mingosgit.josecr.torneoya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.entities.EquipoEntity
import mingosgit.josecr.torneoya.data.entities.PartidoEntity
import mingosgit.josecr.torneoya.repository.EquipoRepository
import mingosgit.josecr.torneoya.repository.PartidoRepository

class CreatePartidoViewModel(
    private val partidoRepository: PartidoRepository,
    private val equipoRepository: EquipoRepository
) : ViewModel() {

    fun crearPartido(
        equipoA: String,
        equipoB: String,
        fecha: String,
        horaInicio: String,
        numeroPartes: Int,
        tiempoPorParte: Int,
        numeroJugadores: Int,
        partidoTempId: Long,
        onFinish: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val equipoAId = equipoRepository.insertEquipo(EquipoEntity(nombre = equipoA))
            val equipoBId = equipoRepository.insertEquipo(EquipoEntity(nombre = equipoB))
            val partido = PartidoEntity(
                id = partidoTempId,
                fecha = fecha,
                horaInicio = horaInicio,
                numeroPartes = numeroPartes,
                tiempoPorParte = tiempoPorParte,
                equipoAId = equipoAId,
                equipoBId = equipoBId,
                numeroJugadores = numeroJugadores
            )
            partidoRepository.insertPartido(partido)
            onFinish()
        }
    }
}
