package mingosgit.josecr.torneoya.viewmodel.partido

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.entities.EquipoEntity
import mingosgit.josecr.torneoya.data.entities.PartidoEntity
import mingosgit.josecr.torneoya.data.entities.PartidoEstado
import mingosgit.josecr.torneoya.repository.EquipoRepository
import mingosgit.josecr.torneoya.repository.PartidoRepository

class CreatePartidoViewModel(
    private val partidoRepository: PartidoRepository,
    private val equipoRepository: EquipoRepository
) : ViewModel() {

    fun crearPartidoYEquipos(
        equipoA: String,
        equipoB: String,
        fecha: String,
        horaInicio: String,
        numeroPartes: Int,
        tiempoPorParte: Int,
        tiempoDescanso: Int,
        numeroJugadores: Int,
        onFinish: (Long, Long, Long) -> Unit = { _, _, _ -> }
    ) {
        viewModelScope.launch {
            val equipoAId = equipoRepository.insertEquipo(EquipoEntity(nombre = equipoA))
            val equipoBId = equipoRepository.insertEquipo(EquipoEntity(nombre = equipoB))
            val partido = PartidoEntity(
                fecha = fecha,
                horaInicio = horaInicio,
                numeroPartes = numeroPartes,
                tiempoPorParte = tiempoPorParte,
                tiempoDescanso = tiempoDescanso,
                equipoAId = equipoAId,
                equipoBId = equipoBId,
                numeroJugadores = numeroJugadores,
                estado = PartidoEstado.PREVIA
            )
            val partidoId = partidoRepository.insertPartido(partido)
            onFinish(partidoId, equipoAId, equipoBId)
        }
    }
}
