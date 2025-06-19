package mingosgit.josecr.torneoya.viewmodel.partido

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.entities.PartidoEquipoJugadorEntity
import mingosgit.josecr.torneoya.repository.JugadorRepository
import mingosgit.josecr.torneoya.repository.PartidoEquipoJugadorRepository
import mingosgit.josecr.torneoya.repository.PartidoRepository
import kotlin.random.Random

class AsignarJugadoresViewModel(
    private val partidoId: Long,
    val numJugadores: Int,
    val equipoAId: Long,
    val equipoBId: Long,
    private val jugadorRepository: JugadorRepository,
    private val partidoRepository: PartidoRepository,
    private val relacionRepository: PartidoEquipoJugadorRepository
) : ViewModel() {

    var equipoAJugadores = mutableStateListOf<String>()
    var equipoBJugadores = mutableStateListOf<String>()
    var listaNombres = mutableStateListOf<String>()
    var modoAleatorio by mutableStateOf(false)
    var equipoSeleccionado by mutableStateOf("A")

    init { setNumJugadoresPorEquipo(numJugadores) }

    fun setNumJugadoresPorEquipo(num: Int) {
        equipoAJugadores.clear()
        equipoBJugadores.clear()
        listaNombres.clear()
        // No rellenar por defecto, solo vacío
    }

    fun cambiarModo(aleatorio: Boolean) { modoAleatorio = aleatorio }

    fun repartirAleatoriamente(nombres: List<String>) {
        val nombresLimpios = nombres.filter { it.isNotBlank() }.shuffled(Random(System.currentTimeMillis()))
        val total = nombresLimpios.size
        val mitad = total / 2

        equipoAJugadores.clear()
        equipoBJugadores.clear()

        if (total % 2 == 0) {
            equipoAJugadores.addAll(nombresLimpios.take(mitad))
            equipoBJugadores.addAll(nombresLimpios.drop(mitad))
        } else {
            val equipoConExtra = if (Random.nextBoolean()) "A" else "B"
            if (equipoConExtra == "A") {
                equipoAJugadores.addAll(nombresLimpios.take(mitad + 1))
                equipoBJugadores.addAll(nombresLimpios.drop(mitad + 1))
            } else {
                equipoAJugadores.addAll(nombresLimpios.take(mitad))
                equipoBJugadores.addAll(nombresLimpios.drop(mitad))
            }
        }
        while (equipoAJugadores.size < numJugadores) equipoAJugadores.add("")
        while (equipoBJugadores.size < numJugadores) equipoBJugadores.add("")
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
}

class AsignarJugadoresViewModelFactory(
    private val partidoId: Long,
    private val numJugadores: Int,
    private val equipoAId: Long,
    private val equipoBId: Long,
    private val jugadorRepository: JugadorRepository,
    private val partidoRepository: PartidoRepository,
    private val relacionRepository: PartidoEquipoJugadorRepository
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
                relacionRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
