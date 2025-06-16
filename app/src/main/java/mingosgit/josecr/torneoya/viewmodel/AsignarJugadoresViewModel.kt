package mingosgit.josecr.torneoya.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.entities.JugadorEntity
import mingosgit.josecr.torneoya.data.entities.PartidoEquipoJugadorEntity
import mingosgit.josecr.torneoya.repository.JugadorRepository
import mingosgit.josecr.torneoya.repository.PartidoRepository
import kotlin.random.Random

class AsignarJugadoresViewModel(
    private val partidoId: Long,
    val numJugadores: Int,
    private val jugadorRepository: JugadorRepository,
    private val partidoRepository: PartidoRepository
) : ViewModel() {

    var equipoAJugadores = mutableStateListOf<String>().apply { repeat(numJugadores) { add("") } }
    var equipoBJugadores = mutableStateListOf<String>().apply { repeat(numJugadores) { add("") } }
    var modoAleatorio by mutableStateOf(false)
    var listaNombres = mutableStateListOf<String>().apply { repeat(numJugadores * 2) { add("") } }
    var equipoSeleccionado by mutableStateOf("A")

    private var manualToAleatorioBackup = List(numJugadores * 2) { "" }
    private var aleatorioToManualA = List(numJugadores) { "" }
    private var aleatorioToManualB = List(numJugadores) { "" }

    fun setNumJugadoresPorEquipo(n: Int) {
        while (equipoAJugadores.size < n) equipoAJugadores.add("")
        while (equipoAJugadores.size > n) equipoAJugadores.removeAt(equipoAJugadores.size - 1)
        while (equipoBJugadores.size < n) equipoBJugadores.add("")
        while (equipoBJugadores.size > n) equipoBJugadores.removeAt(equipoBJugadores.size - 1)
        while (listaNombres.size < n * 2) listaNombres.add("")
        while (listaNombres.size > n * 2) listaNombres.removeAt(listaNombres.size - 1)
    }

    fun cambiarModo(nuevoAleatorio: Boolean) {
        if (nuevoAleatorio == modoAleatorio) return
        if (nuevoAleatorio) {
            // De manual a aleatorio: copiar nombres manuales a listaNombres
            val nombresManual = (equipoAJugadores + equipoBJugadores).toMutableList()
            for (i in listaNombres.indices) {
                listaNombres[i] = nombresManual.getOrNull(i) ?: ""
            }
            manualToAleatorioBackup = nombresManual
        } else {
            // De aleatorio a manual: copiar listaNombres a los equipos, mitad A, mitad B
            val nombres = listaNombres.toList()
            for (i in 0 until numJugadores) {
                equipoAJugadores[i] = nombres.getOrNull(i) ?: ""
            }
            for (i in 0 until numJugadores) {
                equipoBJugadores[i] = nombres.getOrNull(i + numJugadores) ?: ""
            }
            aleatorioToManualA = equipoAJugadores.toList()
            aleatorioToManualB = equipoBJugadores.toList()
        }
        modoAleatorio = nuevoAleatorio
    }

    fun asignarAleatorio(listaNombresParam: List<String>) {
        val shuffled = listaNombresParam.shuffled(Random(System.currentTimeMillis()))
        setNumJugadoresPorEquipo(numJugadores)
        equipoAJugadores.clear()
        equipoBJugadores.clear()
        equipoAJugadores.addAll(shuffled.take(numJugadores))
        equipoBJugadores.addAll(shuffled.drop(numJugadores).take(numJugadores))
    }

    fun guardarEnBD(onFinish: () -> Unit = {}) {
        viewModelScope.launch {
            val nombres = equipoAJugadores + equipoBJugadores
            val jugadoresIds = mutableMapOf<String, Long>()
            for (nombre in nombres) {
                if (nombre.isBlank()) continue
                val id = jugadorRepository.insertJugador(JugadorEntity(nombre = nombre))
                jugadoresIds[nombre] = id
            }
            equipoAJugadores.forEach { nombre ->
                val id = jugadoresIds[nombre] ?: return@forEach
                partidoRepository.asignarJugadorAPartido(
                    PartidoEquipoJugadorEntity(partidoId, "A", id)
                )
            }
            equipoBJugadores.forEach { nombre ->
                val id = jugadoresIds[nombre] ?: return@forEach
                partidoRepository.asignarJugadorAPartido(
                    PartidoEquipoJugadorEntity(partidoId, "B", id)
                )
            }
            onFinish()
        }
    }
}
