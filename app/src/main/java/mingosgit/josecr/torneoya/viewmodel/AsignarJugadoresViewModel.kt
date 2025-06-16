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
    private val equipoAId: Long,
    private val equipoBId: Long,
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
            val nombresManual = (equipoAJugadores + equipoBJugadores).toMutableList()
            for (i in listaNombres.indices) {
                listaNombres[i] = nombresManual.getOrNull(i) ?: ""
            }
            manualToAleatorioBackup = nombresManual
        } else {
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
            // 1. Nombres vacíos → "Jugador X" único (no duplicar)
            val todosNombres = (equipoAJugadores + equipoBJugadores).toMutableList()
            val usados = mutableSetOf<String>()
            var contadorAnon = 1

            for (i in todosNombres.indices) {
                if (todosNombres[i].isBlank()) {
                    // Buscar un nombre "Jugador X" que no esté en usados
                    var nombreGenerado: String
                    do {
                        nombreGenerado = "Jugador $contadorAnon"
                        contadorAnon++
                    } while (usados.contains(nombreGenerado))
                    todosNombres[i] = nombreGenerado
                }
                // Evitar duplicados: si nombre ya existe, añádele sufijo incremental
                var nombreFinal = todosNombres[i]
                var sufijo = 2
                while (usados.contains(nombreFinal)) {
                    nombreFinal = "${todosNombres[i]} ($sufijo)"
                    sufijo++
                }
                todosNombres[i] = nombreFinal
                usados.add(nombreFinal)
            }

            // 2. Guardar jugadores (no duplicar por nombre en este partido)
            val jugadoresIds = mutableMapOf<String, Long>()
            for (nombre in todosNombres) {
                val id = jugadorRepository.insertJugador(JugadorEntity(nombre = nombre))
                jugadoresIds[nombre] = id
            }

            // 3. Relacionar con partido/equipo por ID
            val jugadoresCompletosA = todosNombres.take(numJugadores)
            val jugadoresCompletosB = todosNombres.drop(numJugadores)

            jugadoresCompletosA.forEach { nombre ->
                val id = jugadoresIds[nombre] ?: return@forEach
                partidoRepository.asignarJugadorAPartido(
                    PartidoEquipoJugadorEntity(partidoId, equipoAId, id)
                )
            }
            jugadoresCompletosB.forEach { nombre ->
                val id = jugadoresIds[nombre] ?: return@forEach
                partidoRepository.asignarJugadorAPartido(
                    PartidoEquipoJugadorEntity(partidoId, equipoBId, id)
                )
            }

            // 4. Actualiza las listas en la UI con los nombres finales
            for (i in equipoAJugadores.indices) equipoAJugadores[i] = jugadoresCompletosA[i]
            for (i in equipoBJugadores.indices) equipoBJugadores[i] = jugadoresCompletosB[i]

            onFinish()
        }
    }
}
