package mingosgit.josecr.torneoya.viewmodel.partidoonline

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

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

    var jugadoresExistentes = mutableStateOf<List<JugadorFirebase>>(emptyList())
        private set
    var jugadoresDisponiblesTodos = mutableStateOf<List<JugadorFirebase>>(emptyList())
        private set
    var miUsuario = mutableStateOf<UsuarioFirebaseEntity?>(null)
    var amigos = mutableStateOf<List<AmigoFirebaseEntity>>(emptyList())
    var equipoSeleccionado = mutableStateOf("A")

    // NUEVO: nombre de los equipos
    var equipoANombre by mutableStateOf("Equipo A")
    var equipoBNombre by mutableStateOf("Equipo B")

    fun cargarJugadoresExistentes() {
        viewModelScope.launch {
            jugadoresExistentes.value = partidoFirebaseRepository.obtenerJugadores()
            cargarNombresEquipos()
            cargarUsuarioYAmigos()
            cargarJugadoresPartido()
        }
    }

    private suspend fun cargarNombresEquipos() {
        val partido = partidoFirebaseRepository.obtenerPartido(partidoUid)
        equipoANombre = "Equipo A"
        equipoBNombre = "Equipo B"
        if (partido?.equipoAId?.isNotEmpty() == true) {
            partidoFirebaseRepository.obtenerEquipo(partido.equipoAId)?.let {
                equipoANombre = it.nombre.ifBlank { "Equipo A" }
            }
        }
        if (partido?.equipoBId?.isNotEmpty() == true) {
            partidoFirebaseRepository.obtenerEquipo(partido.equipoBId)?.let {
                equipoBNombre = it.nombre.ifBlank { "Equipo B" }
            }
        }
    }

    private suspend fun cargarUsuarioYAmigos() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        miUsuario.value = usuarioAuthRepository.getUsuarioByUid(uid)
        amigos.value = obtenerListaAmigos()
        val jugadoresList = mutableListOf<JugadorFirebase>()

        // --- CORRECCIÓN SEGURA DEL CAMPO AVATAR ---
        suspend fun getAvatarSafe(usuarioUid: String): Int? {
            val db = FirebaseFirestore.getInstance()
            val doc = db.collection("usuarios").document(usuarioUid).get().await()
            val avatarValue = doc.get("avatar")
            return when (avatarValue) {
                is String -> avatarValue.toIntOrNull()
                is Long -> avatarValue.toInt()
                is Int -> avatarValue
                else -> null
            }
        }

        miUsuario.value?.let {
            jugadoresList.add(
                JugadorFirebase(
                    uid = it.uid,
                    nombre = it.nombreUsuario,
                    email = it.email,
                    avatar = getAvatarSafe(it.uid)
                )
            )
        }
        amigos.value.forEach { amigo ->
            jugadoresList.add(
                JugadorFirebase(
                    uid = amigo.uid,
                    nombre = amigo.nombreUsuario,
                    email = "",
                    avatar = getAvatarSafe(amigo.uid)
                )
            )
        }
        val uidsActuales = jugadoresList.map { it.uid }.toSet()
        jugadoresExistentes.value.forEach { j ->
            if (j.uid !in uidsActuales)
                jugadoresList.add(
                    j.copy(
                        avatar = getAvatarSafe(j.uid)
                    )
                )
        }
        jugadoresDisponiblesTodos.value = jugadoresList
    }

    private suspend fun cargarJugadoresPartido() {
        val partido = partidoFirebaseRepository.obtenerPartido(partidoUid)
        equipoAJugadores.clear()
        equipoBJugadores.clear()
        partido?.jugadoresEquipoA?.forEach { uid ->
            val jugador = jugadoresDisponiblesTodos.value.find { it.uid == uid }
                ?: JugadorFirebase(uid = "", nombre = uid)
            equipoAJugadores.add(jugador)
        }
        partido?.nombresManualEquipoA?.forEach { nombre ->
            equipoAJugadores.add(JugadorFirebase(uid = "", nombre = nombre))
        }
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

    private suspend fun actualizarContadoresPartidosJugados(
        oldA: List<String>,
        oldB: List<String>,
        newA: List<String>,
        newB: List<String>
    ) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
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

    fun guardarEnBD(onFinish: () -> Unit) {
        viewModelScope.launch {
            val equipoA_uids = equipoAJugadores.mapNotNull { if (it.uid.isNotBlank()) it.uid else null }
            val equipoA_nombres = equipoAJugadores.mapNotNull { if (it.uid.isBlank() && it.nombre.isNotBlank()) it.nombre else null }
            val equipoB_uids = equipoBJugadores.mapNotNull { if (it.uid.isNotBlank()) it.uid else null }
            val equipoB_nombres = equipoBJugadores.mapNotNull { if (it.uid.isBlank() && it.nombre.isNotBlank()) it.nombre else null }

            val partido = partidoFirebaseRepository.obtenerPartido(partidoUid)
            val oldA = partido?.jugadoresEquipoA ?: emptyList()
            val oldB = partido?.jugadoresEquipoB ?: emptyList()
            val actuales = partido?.usuariosConAcceso ?: emptyList()
            val nuevosAccesos = (actuales + equipoA_uids + equipoB_uids).distinct()

            // ACTUALIZA CONTADOR partidosJugados
            actualizarContadoresPartidosJugados(
                oldA = oldA,
                oldB = oldB,
                newA = equipoA_uids,
                newB = equipoB_uids
            )

            partidoFirebaseRepository.actualizarJugadoresPartidoOnline(
                partidoUid = partidoUid,
                jugadoresEquipoA = equipoA_uids,
                nombresManualEquipoA = equipoA_nombres,
                jugadoresEquipoB = equipoB_uids,
                nombresManualEquipoB = equipoB_nombres
            )
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("partidos")
                .document(partidoUid)
                .update("usuariosConAcceso", nuevosAccesos)
                .await()
            onFinish()
        }
    }
}
