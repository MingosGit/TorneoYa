package mingosgit.josecr.torneoya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mingosgit.josecr.torneoya.repository.PartidoRepository

class EditPartidoViewModelFactory(
    private val partidoRepository: PartidoRepository,
    private val partidoId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditPartidoViewModel::class.java)) {
            return EditPartidoViewModel(partidoRepository, partidoId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
