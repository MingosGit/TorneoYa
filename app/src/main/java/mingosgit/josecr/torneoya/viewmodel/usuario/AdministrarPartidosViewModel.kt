package mingosgit.josecr.torneoya.viewmodel.usuario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.entities.PartidoEntity
import mingosgit.josecr.torneoya.repository.PartidoRepository

class AdministrarPartidosViewModel(
    private val repository: PartidoRepository
) : ViewModel() {

    private val _partidos = MutableStateFlow<List<PartidoEntity>>(emptyList())
    val partidos: StateFlow<List<PartidoEntity>> = _partidos

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda

    fun setBusqueda(query: String) {
        _busqueda.value = query
        filtrarPartidos()
    }

    fun cargarPartidos() {
        viewModelScope.launch {
            _partidos.value = repository.getAllPartidos()
        }
    }

    private fun filtrarPartidos() {
        viewModelScope.launch {
            val query = _busqueda.value.trim().lowercase()
            val all = repository.getAllPartidos()
            if (query.isBlank()) {
                _partidos.value = all
            } else {
                _partidos.value = all.filter {
                    it.fecha.contains(query, true) ||
                            it.id.toString() == query
                }
            }
        }
    }

    fun actualizarGoles(partido: PartidoEntity, golesA: Int, golesB: Int) {
        viewModelScope.launch {
            repository.actualizarGoles(partido.id, golesA, golesB)
            cargarPartidos()
        }
    }

    class Factory(private val repository: PartidoRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AdministrarPartidosViewModel::class.java)) {
                return AdministrarPartidosViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
