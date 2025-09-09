package mingosgit.josecr.torneoya.viewmodel.usuario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.data.entities.EquipoPredefinidoEntity
import mingosgit.josecr.torneoya.data.entities.JugadorEntity
import mingosgit.josecr.torneoya.repository.EquipoPredefinidoRepository
import mingosgit.josecr.torneoya.repository.JugadorRepository

// ViewModel para crear un equipo predefinido
class CrearEquipoPredefinidoViewModel(
    // Repo para equipos predefinidos
    private val equipoPredefinidoRepository: EquipoPredefinidoRepository,
    // Repo para jugadores
    private val jugadorRepository: JugadorRepository
) : ViewModel() {

    // Estado: nombre del equipo
    private val _nombreEquipo = MutableStateFlow("")
    val nombreEquipo: StateFlow<String> = _nombreEquipo.asStateFlow()

    // Estado: lista de jugadores elegidos
    private val _jugadoresSeleccionados = MutableStateFlow<List<JugadorEntity>>(emptyList())
    val jugadoresSeleccionados: StateFlow<List<JugadorEntity>> = _jugadoresSeleccionados.asStateFlow()

    // Estado: todos los jugadores existentes en BD
    private val _jugadoresExistentes = MutableStateFlow<List<JugadorEntity>>(emptyList())
    val jugadoresExistentes: StateFlow<List<JugadorEntity>> = _jugadoresExistentes.asStateFlow()

    // Estado: indica proceso de creación
    private val _creando = MutableStateFlow(false)
    val creando: StateFlow<Boolean> = _creando.asStateFlow()

    // Estado: mensaje de error
    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error.asStateFlow()

    // Al iniciar, carga todos los jugadores existentes
    init {
        viewModelScope.launch {
            _jugadoresExistentes.value = jugadorRepository.getAll()
        }
    }

    // Actualiza el nombre del equipo
    fun onNombreEquipoChanged(nuevo: String) {
        _nombreEquipo.value = nuevo
    }

    // Añade un jugador existente si no está ya seleccionado
    fun agregarJugadorExistente(jugador: JugadorEntity) {
        if (_jugadoresSeleccionados.value.any { it.id == jugador.id }) return
        _jugadoresSeleccionados.value = _jugadoresSeleccionados.value + jugador
    }

    // Crea (si no existe) y añade un jugador nuevo a la selección
    fun agregarJugadorNuevo(nombre: String) {
        viewModelScope.launch {
            val trimmed = nombre.trim()
            if (trimmed.isBlank()) return@launch
            val id = jugadorRepository.getOrCreateJugador(trimmed)
            val nuevoJugador = jugadorRepository.getById(id)
            nuevoJugador?.let {
                if (_jugadoresSeleccionados.value.any { it.id == nuevoJugador.id }) return@launch
                _jugadoresSeleccionados.value = _jugadoresSeleccionados.value + nuevoJugador
                _jugadoresExistentes.value = jugadorRepository.getAll()
            }
        }
    }

    // Quita un jugador de la selección
    fun quitarJugador(jugador: JugadorEntity) {
        _jugadoresSeleccionados.value = _jugadoresSeleccionados.value.filter { it.id != jugador.id }
    }

    // Valida y crea el equipo; asocia jugadores y navega atrás
    fun crearEquipo(navController: NavController) {
        val nombre = nombreEquipo.value.trim()
        val jugadores = jugadoresSeleccionados.value
        if (nombre.isBlank() || jugadores.isEmpty()) {
            _error.value = "Introduce nombre y al menos un jugador"
            return
        }
        _creando.value = true
        _error.value = ""
        viewModelScope.launch {
            try {
                val equipoId = equipoPredefinidoRepository.insertEquipo(
                    EquipoPredefinidoEntity(nombre = nombre)
                )
                for (jugador in jugadores) {
                    equipoPredefinidoRepository.agregarJugador(equipoId, jugador.id)
                }
                _creando.value = false
                navController.popBackStack()
            } catch (e: Exception) {
                _creando.value = false
                _error.value = "Error al crear equipo"
            }
        }
    }

    // Factory para inyectar repos en el ViewModel
    class Factory(
        private val equipoPredefinidoRepository: EquipoPredefinidoRepository,
        private val jugadorRepository: JugadorRepository
    ) : ViewModelProvider.Factory {
        // Crea la instancia del ViewModel con dependencias
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return CrearEquipoPredefinidoViewModel(
                equipoPredefinidoRepository,
                jugadorRepository
            ) as T
        }
    }
}
