package mingosgit.josecr.torneoya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.entities.PartidoEntity
import mingosgit.josecr.torneoya.data.entities.EquipoEntity
import mingosgit.josecr.torneoya.repository.PartidoRepository
import mingosgit.josecr.torneoya.repository.JugadorRepository
import mingosgit.josecr.torneoya.repository.EquipoRepository

class EditPartidoViewModel(
    private val partidoRepository: PartidoRepository,
    private val jugadorRepository: JugadorRepository,
    private val equipoRepository: EquipoRepository,
    private val partidoId: Long
) : ViewModel() {

    private val _partido = MutableStateFlow<PartidoEntity?>(null)
    val partido: StateFlow<PartidoEntity?> get() = _partido

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> get() = _loading

    private val _eliminado = MutableStateFlow(false)
    val eliminado: StateFlow<Boolean> get() = _eliminado

    private val _guardado = MutableStateFlow(false)
    val guardado: StateFlow<Boolean> get() = _guardado

    private val _jugadoresEquipoA = MutableStateFlow<List<String>>(emptyList())
    val jugadoresEquipoA: StateFlow<List<String>> get() = _jugadoresEquipoA

    private val _jugadoresEquipoB = MutableStateFlow<List<String>>(emptyList())
    val jugadoresEquipoB: StateFlow<List<String>> get() = _jugadoresEquipoB

    private val _jugadoresCargados = MutableStateFlow(false)
    val jugadoresCargados: StateFlow<Boolean> get() = _jugadoresCargados

    init {
        cargarPartido()
        cargarJugadores()
    }

    fun cargarPartido() {
        viewModelScope.launch {
            _loading.value = true
            _partido.value = partidoRepository.getPartidoById(partidoId)
            _loading.value = false
        }
    }

    fun cargarJugadores() {
        viewModelScope.launch {
            val partido = partidoRepository.getPartidoById(partidoId)
            val jugadoresA = partido?.equipoAId?.let {
                partidoRepository.getJugadoresDeEquipoEnPartido(partidoId, it)
            } ?: emptyList()
            val jugadoresB = partido?.equipoBId?.let {
                partidoRepository.getJugadoresDeEquipoEnPartido(partidoId, it)
            } ?: emptyList()
            _jugadoresEquipoA.value = jugadoresA.map { it.nombre }
            _jugadoresEquipoB.value = jugadoresB.map { it.nombre }
            _jugadoresCargados.value = true
        }
    }

    fun actualizarPartido(
        fecha: String,
        horaInicio: String,
        numeroPartes: Int,
        tiempoPorParte: Int
    ) {
        val p = _partido.value ?: return
        val nuevo = p.copy(
            fecha = fecha,
            horaInicio = horaInicio,
            numeroPartes = numeroPartes,
            tiempoPorParte = tiempoPorParte
        )
        viewModelScope.launch {
            partidoRepository.updatePartido(nuevo)
            _partido.value = nuevo
            _guardado.value = true
        }
    }

    fun eliminarPartido() {
        val p = _partido.value ?: return
        viewModelScope.launch {
            partidoRepository.deletePartido(p)
            _eliminado.value = true
        }
    }

    suspend fun getEquipoNombre(equipoId: Long): String? {
        return equipoRepository.getById(equipoId)?.nombre
    }

    suspend fun actualizarEquipoNombre(equipoId: Long, nuevoNombre: String): Boolean {
        val equipo = equipoRepository.getById(equipoId)
        return if (equipo != null) {
            equipoRepository.updateEquipo(equipo.copy(nombre = nuevoNombre.trim()))
            true
        } else {
            false
        }
    }
}
