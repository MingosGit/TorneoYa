package mingosgit.josecr.torneoya.viewmodel.partido

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mingosgit.josecr.torneoya.repository.PartidoRepository
import mingosgit.josecr.torneoya.repository.EquipoRepository

class CreatePartidoViewModelFactory(
    private val partidoRepository: PartidoRepository,
    private val equipoRepository: EquipoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreatePartidoViewModel::class.java)) {
            return CreatePartidoViewModel(partidoRepository, equipoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
