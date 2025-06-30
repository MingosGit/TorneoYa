package mingosgit.josecr.torneoya.viewmodel.partido

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.entities.PartidoEquipoJugadorEntity
import mingosgit.josecr.torneoya.data.entities.JugadorEntity
import mingosgit.josecr.torneoya.repository.JugadorRepository
import mingosgit.josecr.torneoya.repository.PartidoEquipoJugadorRepository
import mingosgit.josecr.torneoya.repository.PartidoRepository
import mingosgit.josecr.torneoya.repository.EquipoPredefinidoRepository
import kotlin.random.Random

class AsignarJugadoresViewModel(
    private val partidoId: Long,
    val numJugadores: Int, // IGNORADO
    val equipoAId: Long,
    val equipoBId: Long,
    private val jugadorRepository: JugadorRepository,
    private val partidoRepository: PartidoRepository,
    private val relacionRepository: PartidoEquipoJugadorRepository,
    private val equipoPredefinidoRepository: EquipoPredefinidoRepository,
    private val equipoAPredefinidoId: Long?,
    private val equipoBPredefinidoId: Long?
) : ViewModel() {

    var equipoAJugadores = mutableStateListOf<String>()
    var equipoBJugadores = mutableStateListOf<String>()
    var listaNombres = mutableStateListOf<String>()
    var modoAleatorio by mutableStateOf(false)
    var equipoSeleccionado by mutableStateOf("A")

    var jugadoresExistentes by mutableStateOf<List<JugadorEntity>>(emptyList())
        private set

    init {
        setNumJugadoresPorEquipo()
        cargarJugadoresExistentes()
        cargarPredefinidosSiAplica()
    }

    fun setNumJugadoresPorEquipo() {
        equipoAJugadores.clear()
        equipoBJugadores.clear()
        listaNombres.clear()
        // NO AGREGUES CAMPOS VACÍOS
    }

    fun cambiarModo(aleatorio: Boolean) { modoAleatorio = aleatorio }

    fun repartirAleatoriamente(nombres: List<String>) {
        val nombresLimpios = nombres.filter { it.isNotBlank() }.shuffled(Random(System.currentTimeMillis()))
        val mitad = nombresLimpios.size / 2

        equipoAJugadores.clear()
        equipoBJugadores.clear()

        if (nombresLimpios.size % 2 == 0) {
            equipoAJugadores.addAll(nombresLimpios.take(mitad))
            equipoBJugadores.addAll(nombresLimpios.drop(mitad))
        } else {
            val extraA = Random.nextBoolean()
            if (extraA) {
                equipoAJugadores.addAll(nombresLimpios.take(mitad + 1))
                equipoBJugadores.addAll(nombresLimpios.drop(mitad + 1))
            } else {
                equipoAJugadores.addAll(nombresLimpios.take(mitad))
                equipoBJugadores.addAll(nombresLimpios.drop(mitad))
            }
        }
        // NO RELLENES MÁS CAMPOS, SOLO LOS QUE TOCAN
    }

    fun guardarEnBD(onFinish: () -> Unit) {
        viewModelScope.launch {
            relacionRepository.eliminarJugadoresDeEquipo(partidoId, equipoAId)
            relacionRepository.eliminarJugadoresDeEquipo(partidoId, equipoBId)

            for (nombre in equipoAJugadores.filter { it.isNotBlank() }) {
                val jugadorId = jugadorRepository.getOrCreateJugador(nombre)
                relacionRepository.insert(
                    PartidoEquipoJugadorEntity(
                        partidoId = partidoId,
                        equipoId = equipoAId,
                        jugadorId = jugadorId
                    )
                )
            }
            for (nombre in equipoBJugadores.filter { it.isNotBlank() }) {
                val jugadorId = jugadorRepository.getOrCreateJugador(nombre)
                relacionRepository.insert(
                    PartidoEquipoJugadorEntity(
                        partidoId = partidoId,
                        equipoId = equipoBId,
                        jugadorId = jugadorId
                    )
                )
            }
            onFinish()
        }
    }

    fun cargarJugadoresExistentes() {
        viewModelScope.launch {
            jugadoresExistentes = jugadorRepository.getAll()
        }
    }

    fun cargarPredefinidosSiAplica() {
        viewModelScope.launch {
            if (equipoAPredefinidoId != null) {
                val eqA = equipoPredefinidoRepository.getEquipoConJugadores(equipoAPredefinidoId)
                equipoAJugadores.clear()
                if (eqA != null) equipoAJugadores.addAll(eqA.jugadores.map { it.nombre })
            }
            if (equipoBPredefinidoId != null) {
                val eqB = equipoPredefinidoRepository.getEquipoConJugadores(equipoBPredefinidoId)
                equipoBJugadores.clear()
                if (eqB != null) equipoBJugadores.addAll(eqB.jugadores.map { it.nombre })
            }
        }
    }

    fun jugadoresDisponiblesManual(equipo: String, idx: Int): List<JugadorEntity> {
        val (jugadoresActuales, jugadoresOtroEquipo) = if (equipo == "A") {
            equipoAJugadores to equipoBJugadores
        } else {
            equipoBJugadores to equipoAJugadores
        }
        val yaElegidosEsteEquipo = jugadoresActuales.withIndex().filter { it.index != idx }.map { it.value }
        val yaElegidosOtroEquipo = jugadoresOtroEquipo.filter { it.isNotBlank() }
        return jugadoresExistentes.filter {
            it.nombre !in yaElegidosEsteEquipo && it.nombre !in yaElegidosOtroEquipo
        }
    }

    fun jugadoresDisponiblesAleatorio(idx: Int): List<JugadorEntity> {
        val yaElegidos = listaNombres.withIndex().filter { it.index != idx }.map { it.value }
        return jugadoresExistentes.filter { jugador -> jugador.nombre !in yaElegidos }
    }
}

class AsignarJugadoresViewModelFactory(
    private val partidoId: Long,
    private val numJugadores: Int, // SE IGNORA
    private val equipoAId: Long,
    private val equipoBId: Long,
    private val jugadorRepository: JugadorRepository,
    private val partidoRepository: PartidoRepository,
    private val relacionRepository: PartidoEquipoJugadorRepository,
    private val equipoPredefinidoRepository: EquipoPredefinidoRepository,
    private val equipoAPredefinidoId: Long?,
    private val equipoBPredefinidoId: Long?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AsignarJugadoresViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AsignarJugadoresViewModel(
                partidoId,
                numJugadores,
                equipoAId,
                equipoBId,
                jugadorRepository,
                partidoRepository,
                relacionRepository,
                equipoPredefinidoRepository,
                equipoAPredefinidoId,
                equipoBPredefinidoId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
