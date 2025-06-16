package mingosgit.josecr.torneoya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.entities.PartidoEntity
import mingosgit.josecr.torneoya.repository.PartidoRepository

class CreatePartidoViewModel(
    private val partidoRepository: PartidoRepository
) : ViewModel() {

    fun crearPartido(partido: PartidoEntity) {
        viewModelScope.launch {
            partidoRepository.insertPartido(partido)
        }
    }
}
