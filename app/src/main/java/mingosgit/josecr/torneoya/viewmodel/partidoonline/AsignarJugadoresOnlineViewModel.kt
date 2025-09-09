package mingosgit.josecr.torneoya.viewmodel.partidoonline

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mingosgit.josecr.torneoya.data.firebase.AmigoFirebaseEntity
import mingosgit.josecr.torneoya.data.entities.UsuarioFirebaseEntity
import mingosgit.josecr.torneoya.data.firebase.JugadorFirebase
import mingosgit.josecr.torneoya.data.firebase.NotificacionFirebase
import mingosgit.josecr.torneoya.data.firebase.NotificacionFirebaseRepository
import mingosgit.josecr.torneoya.data.firebase.PartidoFirebaseRepository
import mingosgit.josecr.torneoya.repository.UsuarioAuthRepository
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class AsignarJugadoresOnlineViewModel(
    private val partidoUid: String,
    private val equipoAUid: String,
    private val equipoBUid: String,
    private val partidoFirebaseRepository: PartidoFirebaseRepository,
    private val usuarioAuthRepository: UsuarioAuthRepository,
    private val obtenerListaAmigos: suspend () -> List<AmigoFirebaseEntity>,
    private val notificacionRepository: NotificacionFirebaseRepository = NotificacionFirebaseRepository()
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

    // Cambia entre modo manual y aleatorio.
    fun cambiarModo(aleatorio: Boolean) { modoAleatorio = aleatorio }

    // Actualiza en Firebase el contador "partidosJugados" según altas/bajas de jugadores.
    suspend fun actualizarContadoresPartidosJugados(
        oldA: List<String>,
        oldB: List<String>,
        newA: List<String>,
        newB: List<String>
    ) {
        val db = FirebaseFirestore.getInstance()
        val antes = (oldA + oldB).toSet()
        val despues = (newA + newB).toSet()

        val añadidos = despues - antes
        val eliminados = antes - despues

        añadidos.forEach { uid ->
            db.collection("usuarios").document(uid)
                .update("partidosJugados", FieldValue.increment(1))
                .await()
        }
        eliminados.forEach { uid ->
            db.collection("usuarios").document(uid)
                .update("partidosJugados", FieldValue.increment(-1))
                .await()
        }
    }

    // Reparte la lista en dos equipos al azar, equilibrando el número.
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

    // Guarda en Firestore los jugadores de cada equipo, actualiza accesos y lanza notificaciones a nuevos asignados.
    fun guardarEnBD(onFinish: () -> Unit) {
        viewModelScope.launch {
            val equipoA_uids = equipoAJugadores.mapNotNull { if (it.uid.isNotBlank()) it.uid else null }
            val equipoA_nombres = equipoAJugadores.mapNotNull { if (it.uid.isBlank() && it.nombre.isNotBlank()) it.nombre else null }
            val equipoB_uids = equipoBJugadores.mapNotNull { if (it.uid.isNotBlank()) it.uid else null }
            val equipoB_nombres = equipoBJugadores.mapNotNull { if (it.uid.isBlank() && it.nombre.isNotBlank()) it.nombre else null }

            val partido = partidoFirebaseRepository.obtenerPartido(partidoUid)
            val actuales = partido?.usuariosConAcceso ?: emptyList()
            val oldA = partido?.jugadoresEquipoA ?: emptyList()
            val oldB = partido?.jugadoresEquipoB ?: emptyList()

            val nuevosAccesos = (actuales + equipoA_uids + equipoB_uids).distinct()

            actualizarContadoresPartidosJugados(
                oldA = oldA,
                oldB = oldB,
                newA = equipoA_uids,
                newB = equipoB_uids
            )

            // Guardar jugadores y actualizar accesos del partido.
            partidoFirebaseRepository.actualizarJugadoresPartidoOnline(
                partidoUid = partidoUid,
                jugadoresEquipoA = equipoA_uids,
                nombresManualEquipoA = equipoA_nombres,
                jugadoresEquipoB = equipoB_uids,
                nombresManualEquipoB = equipoB_nombres
            )

            FirebaseFirestore.getInstance()
                .collection("partidos")
                .document(partidoUid)
                .update("usuariosConAcceso", nuevosAccesos)
                .await()

            // Notifica solo a quienes se añaden ahora.
            val jugadoresAgregados = (equipoA_uids + equipoB_uids).toSet() - (oldA + oldB).toSet()
            jugadoresAgregados.forEach { uidJugador ->
                lanzarNotificacionAsignacion(uidJugador)
            }

            onFinish()
        }
    }

    // Crea y guarda una notificación de asignación de jugador.
    private suspend fun lanzarNotificacionAsignacion(usuarioUid: String) {
        val usuario = usuarioAuthRepository.getUsuarioByUid(usuarioUid) ?: return
        val notificacion = NotificacionFirebase(
            tipo = "asignacion_jugador",
            titulo = "Asignación a partido",
            mensaje = "Has sido asignado como jugador en el partido $partidoUid.",
            fechaHora = Timestamp.now(),
            usuarioUid = usuarioUid
        )
        notificacionRepository.agregarNotificacion(notificacion)
    }

    // Carga desde repositorio la lista base de jugadores y después usuario+amigos.
    fun cargarJugadoresExistentes() {
        viewModelScope.launch {
            jugadoresExistentes = partidoFirebaseRepository.obtenerJugadores()
            cargarUsuarioYAmigos()
        }
    }

    // Monta la lista de disponibles combinando mi usuario, amigos y jugadores ya existentes (evitando duplicados).
    private suspend fun cargarUsuarioYAmigos() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        miUsuario = usuarioAuthRepository.getUsuarioByUid(uid)
        amigos = obtenerListaAmigos()
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
        val uidsActuales = jugadoresList.map { it.uid }.toSet()
        jugadoresExistentes.forEach { j ->
            if (j.uid !in uidsActuales)
                jugadoresList.add(j)
        }
        jugadoresDisponiblesTodos = jugadoresList
    }

    // Devuelve los jugadores disponibles para una posición manual, excluyendo los ya elegidos en ambos equipos.
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

    // Devuelve los jugadores disponibles para modo aleatorio evitando duplicados en la lista de nombres.
    fun jugadoresDisponiblesAleatorio(idx: Int): List<JugadorFirebase> {
        val yaElegidos = listaNombres.withIndex().filter { it.index != idx }.map { it.value.uid }
        return jugadoresDisponiblesTodos.filter { it.uid !in yaElegidos }
    }

    // Se añade a sí mismo al equipo indicado si aún no está.
    fun agregarmeComoJugador(equipo: String) {
        val yo = jugadoresDisponiblesTodos.firstOrNull { it.uid == miUsuario?.uid } ?: return
        when (equipo) {
            "A" -> if (equipoAJugadores.none { it.uid == yo.uid }) equipoAJugadores.add(yo)
            "B" -> if (equipoBJugadores.none { it.uid == yo.uid }) equipoBJugadores.add(yo)
        }
    }

    // Asigna un amigo por su uid al equipo indicado si no está previamente.
    fun asignarAmigoPorUid(uid: String, equipo: String) {
        val amigo = jugadoresDisponiblesTodos.firstOrNull { it.uid == uid } ?: return
        when (equipo) {
            "A" -> if (equipoAJugadores.none { it.uid == amigo.uid }) equipoAJugadores.add(amigo)
            "B" -> if (equipoBJugadores.none { it.uid == amigo.uid }) equipoBJugadores.add(amigo)
        }
    }
}

// Extensión: obtiene de Firestore el usuario por uid y lo convierte a entidad.
suspend fun UsuarioAuthRepository.getUsuarioByUid(uid: String): UsuarioFirebaseEntity? {
    val snap = this.firestore.collection("usuarios").document(uid).get().await()
    return snap.toObject(UsuarioFirebaseEntity::class.java)
}
