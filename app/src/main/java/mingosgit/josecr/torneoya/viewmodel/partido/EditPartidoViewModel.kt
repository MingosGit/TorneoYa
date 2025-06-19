package mingosgit.josecr.torneoya.viewmodel.partido

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.entities.PartidoEntity
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

    // Nombre local en memoria de los equipos para refrescar Compose al editar
    private val _nombreEquipoA = MutableStateFlow<String?>(null)
    val nombreEquipoA: StateFlow<String?> get() = _nombreEquipoA

    private val _nombreEquipoB = MutableStateFlow<String?>(null)
    val nombreEquipoB: StateFlow<String?> get() = _nombreEquipoB

    init {
        cargarPartido()
        cargarJugadores()
    }

    fun cargarPartido() {
        viewModelScope.launch {
            _loading.value = true
            val partidoEntity = partidoRepository.getPartidoById(partidoId)
            _partido.value = partidoEntity
            partidoEntity?.let {
                _nombreEquipoA.value = equipoRepository.getById(it.equipoAId)?.nombre
                _nombreEquipoB.value = equipoRepository.getById(it.equipoBId)?.nombre
            }
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
        // Usa el nombre guardado si ya fue editado
        return if (_partido.value?.equipoAId == equipoId) {
            _nombreEquipoA.value ?: equipoRepository.getById(equipoId)?.nombre
        } else if (_partido.value?.equipoBId == equipoId) {
            _nombreEquipoB.value ?: equipoRepository.getById(equipoId)?.nombre
        } else {
            equipoRepository.getById(equipoId)?.nombre
        }
    }

    suspend fun actualizarEquipoNombre(equipoId: Long, nuevoNombre: String): Boolean {
        val equipo = equipoRepository.getById(equipoId)
        return if (equipo != null) {
            val actualizado = equipo.copy(nombre = nuevoNombre.trim())
            equipoRepository.updateEquipo(actualizado)
            // Relee siempre de la BD para reflejar persistencia real
            val actualizadoReal = equipoRepository.getById(equipoId)
            if (_partido.value?.equipoAId == equipoId) _nombreEquipoA.value = actualizadoReal?.nombre
            if (_partido.value?.equipoBId == equipoId) _nombreEquipoB.value = actualizadoReal?.nombre
            true
        } else {
            false
        }
    }

}
