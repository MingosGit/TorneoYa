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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.tasks.await

class AsignarJugadoresOnlineViewModel(
    private val partidoUid: String,
    private val equipoAUid: String,
    private val equipoBUid: String,
    private val partidoFirebaseRepository: PartidoFirebaseRepository,
    private val usuarioAuthRepository: UsuarioAuthRepository,
    private val obtenerListaAmigos: suspend () -> List<AmigoFirebaseEntity>
) : ViewModel() {

    var equipoAJugadores = mutableStateListOf<JugadorFirebase>()
    var equipoBJugadores = mutableStateListOf<JugadorFirebase>()
    var listaNombres = mutableStateListOf<JugadorFirebase>()
    var modoAleatorio by mutableStateOf(false)
    var equipoSeleccionado by mutableStateOf("A")

    var jugadoresExistentes by mutableStateOf<List<JugadorFirebase>>(emptyList())
        private set

    var jugadoresDisponiblesTodos by mutableStateOf<List<JugadorFirebase>>(emptyList())
        private set

    var miUsuario: UsuarioFirebaseEntity? by mutableStateOf(null)
    var amigos: List<AmigoFirebaseEntity> by mutableStateOf(emptyList())

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
            // Separar jugadores con uid de los manuales
            val equipoA_uids = equipoAJugadores.mapNotNull { if (it.uid.isNotBlank()) it.uid else null }
            val equipoA_nombres = equipoAJugadores.mapNotNull { if (it.uid.isBlank() && it.nombre.isNotBlank()) it.nombre else null }

            val equipoB_uids = equipoBJugadores.mapNotNull { if (it.uid.isNotBlank()) it.uid else null }
            val equipoB_nombres = equipoBJugadores.mapNotNull { if (it.uid.isBlank() && it.nombre.isNotBlank()) it.nombre else null }

            partidoFirebaseRepository.actualizarJugadoresPartidoOnline(
                partidoUid = partidoUid,
                jugadoresEquipoA = equipoA_uids,
                nombresManualEquipoA = equipoA_nombres,
                jugadoresEquipoB = equipoB_uids,
                nombresManualEquipoB = equipoB_nombres
            )
            onFinish()
        }
    }


    fun cargarJugadoresExistentes() {
        viewModelScope.launch {
            jugadoresExistentes = partidoFirebaseRepository.obtenerJugadores()
            cargarUsuarioYAmigos()
        }
    }

    private suspend fun cargarUsuarioYAmigos() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        // Obtén mi usuario
        miUsuario = usuarioAuthRepository.getUsuarioByUid(uid)
        // Obtén amigos
        amigos = obtenerListaAmigos()
        // Arma la lista completa de seleccionables
        val jugadoresList = mutableListOf<JugadorFirebase>()
        miUsuario?.let {
            jugadoresList.add(
                JugadorFirebase(
                    uid = it.uid,
                    nombre = it.nombreUsuario,
                    email = it.email
                )
            )
        }
        amigos.forEach { amigo ->
            jugadoresList.add(
                JugadorFirebase(
                    uid = amigo.uid,
                    nombre = amigo.nombreUsuario,
                    email = ""
                )
            )
        }
        // Agrega también los jugadores "existentes" ya disponibles en la colección jugadores
        val uidsActuales = jugadoresList.map { it.uid }.toSet()
        jugadoresExistentes.forEach { j ->
            if (j.uid !in uidsActuales)
                jugadoresList.add(j)
        }
        jugadoresDisponiblesTodos = jugadoresList
    }

    fun jugadoresDisponiblesManual(equipo: String, idx: Int): List<JugadorFirebase> {
        val (jugadoresActuales, jugadoresOtroEquipo) = if (equipo == "A") {
            equipoAJugadores to equipoBJugadores
        } else {
            equipoBJugadores to equipoAJugadores
        }
        val yaElegidosEsteEquipo = jugadoresActuales.withIndex().filter { it.index != idx }.map { it.value.uid }
        val yaElegidosOtroEquipo = jugadoresOtroEquipo.map { it.uid }
        return jugadoresDisponiblesTodos.filter {
            it.uid !in yaElegidosEsteEquipo && it.uid !in yaElegidosOtroEquipo
        }
    }

    fun jugadoresDisponiblesAleatorio(idx: Int): List<JugadorFirebase> {
        val yaElegidos = listaNombres.withIndex().filter { it.index != idx }.map { it.value.uid }
        return jugadoresDisponiblesTodos.filter { it.uid !in yaElegidos }
    }

    fun agregarmeComoJugador(equipo: String) {
        val yo = jugadoresDisponiblesTodos.firstOrNull { it.uid == miUsuario?.uid } ?: return
        when (equipo) {
            "A" -> if (equipoAJugadores.none { it.uid == yo.uid }) equipoAJugadores.add(yo)
            "B" -> if (equipoBJugadores.none { it.uid == yo.uid }) equipoBJugadores.add(yo)
        }
    }
    fun asignarAmigoPorUid(uid: String, equipo: String) {
        val amigo = jugadoresDisponiblesTodos.firstOrNull { it.uid == uid } ?: return
        when (equipo) {
            "A" -> if (equipoAJugadores.none { it.uid == amigo.uid }) equipoAJugadores.add(amigo)
            "B" -> if (equipoBJugadores.none { it.uid == amigo.uid }) equipoBJugadores.add(amigo)
        }
    }
}

suspend fun UsuarioAuthRepository.getUsuarioByUid(uid: String): UsuarioFirebaseEntity? {
    val snap = this.firestore.collection("usuarios").document(uid).get().await()
    return snap.toObject(UsuarioFirebaseEntity::class.java)
}
