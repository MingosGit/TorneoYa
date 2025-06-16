package mingosgit.josecr.torneoya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mingosgit.josecr.torneoya.repository.PartidoRepository

class PartidoViewModelFactory(
    private val repository: PartidoRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PartidoViewModel::class.java)) {
            return PartidoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
