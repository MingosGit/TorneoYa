package mingosgit.josecr.torneoya.viewmodel

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
    var equipoAJugadores: MutableList<String> = MutableList(numJugadores) { "" }
    var equipoBJugadores: MutableList<String> = MutableList(numJugadores) { "" }

    // Asignación aleatoria
    fun asignarAleatorio(listaNombres: List<String>) {
        val shuffled = listaNombres.shuffled(Random(System.currentTimeMillis()))
        equipoAJugadores = shuffled.take(numJugadores).toMutableList()
        equipoBJugadores = shuffled.drop(numJugadores).take(numJugadores).toMutableList()
    }

    // Guardar todos los jugadores a la BBDD y relacionarlos al partido
    fun guardarEnBD(onFinish: () -> Unit = {}) {
        viewModelScope.launch {
            val nombres = equipoAJugadores + equipoBJugadores
            val jugadoresIds = mutableMapOf<String, Long>()
            // Inserta si no existe ese nombre
            for (nombre in nombres) {
                if (nombre.isBlank()) continue
                val id = jugadorRepository.insertJugador(JugadorEntity(nombre = nombre))
                jugadoresIds[nombre] = id
            }
            // Relaciona jugadores a partido
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
