package mingosgit.josecr.torneoya.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.repository.PartidoRepository
import mingosgit.josecr.torneoya.repository.EquipoRepository
import mingosgit.josecr.torneoya.repository.UsuarioLocalRepository
import mingosgit.josecr.torneoya.repository.JugadorRepository

data class HomeUiState(
    val nombreUsuario: String = "",
    val partidosTotales: Int = 0,
    val equiposTotales: Int = 0,
    val jugadoresTotales: Int = 0
)

class HomeViewModel(
    private val usuarioLocalRepository: UsuarioLocalRepository,
    private val partidoRepository: PartidoRepository,
    private val equipoRepository: EquipoRepository,
    private val jugadorRepository: JugadorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        viewModelScope.launch {
            val usuario = usuarioLocalRepository.getUsuario()
            val partidos = partidoRepository.getAllPartidos()
            val equipos = equipoRepository.getAll()
            val jugadores = jugadorRepository.getAll()

            _uiState.value = HomeUiState(
                nombreUsuario = usuario?.nombre ?: "Usuario",
                partidosTotales = partidos.size,
                equiposTotales = equipos.size,
                jugadoresTotales = jugadores.size
            )
        }
    }

    companion object {
        fun Factory(
            usuarioLocalRepository: UsuarioLocalRepository,
            partidoRepository: PartidoRepository,
            equipoRepository: EquipoRepository,
            jugadorRepository: JugadorRepository
        ) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(
                    usuarioLocalRepository,
                    partidoRepository,
                    equipoRepository,
                    jugadorRepository
                ) as T
            }
        }
    }
}
