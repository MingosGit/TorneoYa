package mingosgit.josecr.torneoya.viewmodel.partidoonline

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.firebase.JugadorFirebase
import mingosgit.josecr.torneoya.data.firebase.PartidoFirebaseRepository
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


class AsignarJugadoresOnlineViewModel(
    private val partidoUid: String,
    private val equipoAUid: String,
    private val equipoBUid: String,
    private val partidoFirebaseRepository: PartidoFirebaseRepository
) : ViewModel() {

    var equipoAJugadores = mutableStateListOf<JugadorFirebase>()
    var equipoBJugadores = mutableStateListOf<JugadorFirebase>()
    var listaNombres = mutableStateListOf<JugadorFirebase>()
    var modoAleatorio by mutableStateOf(false)
    var equipoSeleccionado by mutableStateOf("A")

    var jugadoresExistentes by mutableStateOf<List<JugadorFirebase>>(emptyList())
        private set

    fun cambiarModo(aleatorio: Boolean) { modoAleatorio = aleatorio }

    fun repartirAleatoriamente(jugadores: List<JugadorFirebase>) {
        val jugadoresLimpios = jugadores.filter { it.nombre.isNotBlank() }.shuffled()
        val mitad = jugadoresLimpios.size / 2
        equipoAJugadores.clear()
        equipoBJugadores.clear()
        if (jugadoresLimpios.size % 2 == 0) {
            equipoAJugadores.addAll(jugadoresLimpios.take(mitad))
            equipoBJugadores.addAll(jugadoresLimpios.drop(mitad))
        } else {
            equipoAJugadores.addAll(jugadoresLimpios.take(mitad + 1))
            equipoBJugadores.addAll(jugadoresLimpios.drop(mitad + 1))
        }
    }

    fun guardarEnBD(onFinish: () -> Unit) {
        viewModelScope.launch {
            val jugadoresUids = equipoAJugadores.map { it.uid } + equipoBJugadores.map { it.uid }
            partidoFirebaseRepository.actualizarJugadoresPartido(partidoUid, jugadoresUids)
            onFinish()
        }
    }

    fun cargarJugadoresExistentes() {
        viewModelScope.launch {
            jugadoresExistentes = partidoFirebaseRepository.obtenerJugadores()
        }
    }

    fun jugadoresDisponiblesManual(equipo: String, idx: Int): List<JugadorFirebase> {
        val (jugadoresActuales, jugadoresOtroEquipo) = if (equipo == "A") {
            equipoAJugadores to equipoBJugadores
        } else {
            equipoBJugadores to equipoAJugadores
        }
        val yaElegidosEsteEquipo = jugadoresActuales.withIndex().filter { it.index != idx }.map { it.value.uid }
        val yaElegidosOtroEquipo = jugadoresOtroEquipo.map { it.uid }
        return jugadoresExistentes.filter {
            it.uid !in yaElegidosEsteEquipo && it.uid !in yaElegidosOtroEquipo
        }
    }

    fun jugadoresDisponiblesAleatorio(idx: Int): List<JugadorFirebase> {
        val yaElegidos = listaNombres.withIndex().filter { it.index != idx }.map { it.value.uid }
        return jugadoresExistentes.filter { it.uid !in yaElegidos }
    }
}
