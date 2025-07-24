package mingosgit.josecr.torneoya.viewmodel.partidoonline

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.firebase.JugadorFirebase
import mingosgit.josecr.torneoya.data.firebase.PartidoFirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import mingosgit.josecr.torneoya.data.entities.UsuarioFirebaseEntity
import mingosgit.josecr.torneoya.data.entities.AmigoFirebaseEntity
import mingosgit.josecr.torneoya.repository.UsuarioAuthRepository
import kotlinx.coroutines.tasks.await

class AdministrarJugadoresOnlineViewModel(
    private val partidoUid: String,
    private val equipoAUid: String,
    private val equipoBUid: String,
    private val partidoFirebaseRepository: PartidoFirebaseRepository,
    private val usuarioAuthRepository: UsuarioAuthRepository,
    private val obtenerListaAmigos: suspend () -> List<AmigoFirebaseEntity>
) : ViewModel() {

    var equipoAJugadores = mutableStateListOf<JugadorFirebase>()
    var equipoBJugadores = mutableStateListOf<JugadorFirebase>()

    // NO uses by, solo .value y .value= en ViewModel
    var jugadoresExistentes = mutableStateOf<List<JugadorFirebase>>(emptyList())
        private set
    var jugadoresDisponiblesTodos = mutableStateOf<List<JugadorFirebase>>(emptyList())
        private set
    var miUsuario = mutableStateOf<UsuarioFirebaseEntity?>(null)
    var amigos = mutableStateOf<List<AmigoFirebaseEntity>>(emptyList())
    var equipoSeleccionado = mutableStateOf("A")

    fun cargarJugadoresExistentes() {
        viewModelScope.launch {
            jugadoresExistentes.value = partidoFirebaseRepository.obtenerJugadores()
            cargarUsuarioYAmigos()
            cargarJugadoresPartido()
        }
    }

    private suspend fun cargarUsuarioYAmigos() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        miUsuario.value = usuarioAuthRepository.getUsuarioByUid(uid)
        amigos.value = obtenerListaAmigos()
        val jugadoresList = mutableListOf<JugadorFirebase>()
        miUsuario.value?.let {
            jugadoresList.add(
                JugadorFirebase(
                    uid = it.uid,
                    nombre = it.nombreUsuario,
                    email = it.email
                )
            )
        }
        amigos.value.forEach { amigo ->
            jugadoresList.add(
                JugadorFirebase(
                    uid = amigo.uid,
                    nombre = amigo.nombreUsuario,
                    email = ""
                )
            )
        }
        val uidsActuales = jugadoresList.map { it.uid }.toSet()
        jugadoresExistentes.value.forEach { j ->
            if (j.uid !in uidsActuales)
                jugadoresList.add(j)
        }
        jugadoresDisponiblesTodos.value = jugadoresList
    }

    private suspend fun cargarJugadoresPartido() {
        val partido = partidoFirebaseRepository.obtenerPartido(partidoUid)
        equipoAJugadores.clear()
        equipoBJugadores.clear()
        // Jugadores Equipo A
        partido?.jugadoresEquipoA?.forEach { uid ->
            val jugador = jugadoresDisponiblesTodos.value.find { it.uid == uid }
                ?: JugadorFirebase(uid = "", nombre = uid)
            equipoAJugadores.add(jugador)
        }
        partido?.nombresManualEquipoA?.forEach { nombre ->
            equipoAJugadores.add(JugadorFirebase(uid = "", nombre = nombre))
        }
        // Jugadores Equipo B
        partido?.jugadoresEquipoB?.forEach { uid ->
            val jugador = jugadoresDisponiblesTodos.value.find { it.uid == uid }
                ?: JugadorFirebase(uid = "", nombre = uid)
            equipoBJugadores.add(jugador)
        }
        partido?.nombresManualEquipoB?.forEach { nombre ->
            equipoBJugadores.add(JugadorFirebase(uid = "", nombre = nombre))
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
        return jugadoresDisponiblesTodos.value.filter {
            it.uid !in yaElegidosEsteEquipo && it.uid !in yaElegidosOtroEquipo
        }
    }

    fun agregarJugador(jugador: JugadorFirebase, equipo: String) {
        if (equipo == "A") equipoAJugadores.add(jugador)
        else equipoBJugadores.add(jugador)
    }

    fun eliminarJugador(idx: Int, equipo: String) {
        if (equipo == "A") equipoAJugadores.removeAt(idx)
        else equipoBJugadores.removeAt(idx)
    }

    fun cambiarNombreJugador(idx: Int, equipo: String, nuevo: String) {
        if (equipo == "A") equipoAJugadores[idx] = equipoAJugadores[idx].copy(nombre = nuevo)
        else equipoBJugadores[idx] = equipoBJugadores[idx].copy(nombre = nuevo)
    }

    fun guardarEnBD(onFinish: () -> Unit) {
        viewModelScope.launch {
            val equipoA_uids = equipoAJugadores.mapNotNull { if (it.uid.isNotBlank()) it.uid else null }
            val equipoA_nombres = equipoAJugadores.mapNotNull { if (it.uid.isBlank() && it.nombre.isNotBlank()) it.nombre else null }
            val equipoB_uids = equipoBJugadores.mapNotNull { if (it.uid.isNotBlank()) it.uid else null }
            val equipoB_nombres = equipoBJugadores.mapNotNull { if (it.uid.isBlank() && it.nombre.isNotBlank()) it.nombre else null }

            // Accesos actuales
            val partido = partidoFirebaseRepository.obtenerPartido(partidoUid)
            val actuales = partido?.usuariosConAcceso ?: emptyList()
            val nuevosAccesos = (actuales + equipoA_uids + equipoB_uids).distinct()

            // Guarda jugadores
            partidoFirebaseRepository.actualizarJugadoresPartidoOnline(
                partidoUid = partidoUid,
                jugadoresEquipoA = equipoA_uids,
                nombresManualEquipoA = equipoA_nombres,
                jugadoresEquipoB = equipoB_uids,
                nombresManualEquipoB = equipoB_nombres
            )
            // Solo agrega nuevos accesos. No elimina
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("partidos")
                .document(partidoUid)
                .update("usuariosConAcceso", nuevosAccesos)
                .await()
            onFinish()
        }
    }
}
