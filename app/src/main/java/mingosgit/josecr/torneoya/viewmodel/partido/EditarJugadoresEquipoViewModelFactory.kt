package mingosgit.josecr.torneoya.viewmodel.partido

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mingosgit.josecr.torneoya.repository.JugadorRepository
import mingosgit.josecr.torneoya.repository.PartidoEquipoJugadorRepository
import mingosgit.josecr.torneoya.repository.EquipoRepository

class EditarJugadoresEquipoViewModelFactory(
    private val partidoId: Long,
    private val equipoAId: Long,
    private val equipoBId: Long,
    private val jugadorRepository: JugadorRepository,
    private val relacionRepository: PartidoEquipoJugadorRepository,
    private val equipoRepository: EquipoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditarJugadoresEquipoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditarJugadoresEquipoViewModel(
                partidoId,
                equipoAId,
                equipoBId,
                jugadorRepository,
                relacionRepository,
                equipoRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
