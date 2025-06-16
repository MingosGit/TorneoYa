package mingosgit.josecr.torneoya.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.entities.PartidoEquipoJugadorEntity
import mingosgit.josecr.torneoya.repository.JugadorRepository
import mingosgit.josecr.torneoya.repository.PartidoEquipoJugadorRepository
import mingosgit.josecr.torneoya.repository.PartidoRepository

class AsignarJugadoresViewModel(
    private val partidoId: Long,
    val numJugadores: Int,
    private val equipoAId: Long,
    private val equipoBId: Long,
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
        repeat(num) { equipoAJugadores.add("") }
        repeat(num) { equipoBJugadores.add("") }
        repeat(num * 2) { listaNombres.add("") }
    }

    fun cambiarModo(aleatorio: Boolean) { modoAleatorio = aleatorio }

    fun asignarAleatorio(nombres: List<String>) {
        val mezclados = nombres.shuffled()
        equipoAJugadores.clear()
        equipoBJugadores.clear()
        equipoAJugadores.addAll(mezclados.take(numJugadores))
        equipoBJugadores.addAll(mezclados.drop(numJugadores).take(numJugadores))
    }

    fun guardarEnBD(onFinish: () -> Unit) {
        viewModelScope.launch {
            // Guarda para A
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
            // Guarda para B
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
