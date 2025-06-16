package mingosgit.josecr.torneoya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mingosgit.josecr.torneoya.repository.PartidoRepository
import mingosgit.josecr.torneoya.repository.JugadorRepository

class EditPartidoViewModelFactory(
    private val partidoRepository: PartidoRepository,
    private val jugadorRepository: JugadorRepository,
    private val partidoId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditPartidoViewModel::class.java)) {
            return EditPartidoViewModel(partidoRepository, jugadorRepository, partidoId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
