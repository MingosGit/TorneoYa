package mingosgit.josecr.torneoya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mingosgit.josecr.torneoya.repository.PartidoRepository

class CreatePartidoViewModelFactory(
    private val partidoRepository: PartidoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreatePartidoViewModel::class.java)) {
            return CreatePartidoViewModel(partidoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
