// mingosgit.josecr.torneoya.viewmodel.AsignarJugadoresViewModelFactory.kt
package mingosgit.josecr.torneoya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mingosgit.josecr.torneoya.repository.JugadorRepository
import mingosgit.josecr.torneoya.repository.PartidoRepository

class AsignarJugadoresViewModelFactory(
    private val partidoId: Long,
    private val numJugadores: Int,
    private val jugadorRepository: JugadorRepository,
    private val partidoRepository: PartidoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AsignarJugadoresViewModel::class.java)) {
            return AsignarJugadoresViewModel(
                partidoId,
                numJugadores,
                jugadorRepository,
                partidoRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
