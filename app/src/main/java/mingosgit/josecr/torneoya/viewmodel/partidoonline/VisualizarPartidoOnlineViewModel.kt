// VisualizarPartidoOnlineViewModel.kt
package mingosgit.josecr.torneoya.viewmodel.partidoonline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mingosgit.josecr.torneoya.data.firebase.PartidoFirebaseRepository
import mingosgit.josecr.torneoya.data.firebase.ComentarioFirebase
import mingosgit.josecr.torneoya.data.firebase.EncuestaFirebase
import mingosgit.josecr.torneoya.data.firebase.EquipoFirebase
import mingosgit.josecr.torneoya.data.firebase.GoleadorFirebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// ---------- UI STATE PRINCIPAL (ya existente) ----------
data class VisualizarPartidoOnlineUiState(
    val nombreEquipoA: String = "",
    val nombreEquipoB: String = "",
    val jugadoresEquipoA: List<String> = emptyList(),
    val jugadoresEquipoB: List<String> = emptyList(),
    val estado: String = "",
    val minutoActual: String = "",
    val parteActual: Int = 0,
    val partesTotales: Int = 0,
    val golesEquipoA: Int = 0,
    val golesEquipoB: Int = 0
)

// ---------- MODELOS AUXILIARES (ya existentes + eventos) ----------
data class ComentarioOnlineConVotos(
    val comentario: ComentarioFirebase,
    val avatar: Int?,
    val likes: Int,
    val dislikes: Int,
    val miVoto: Int?
)

data class EncuestaOnlineConResultadosConAvatar(
    val encuesta: EncuestaFirebase,
    val votos: List<Int>,
    val avatar: Int?
)

data class VisualizarPartidoOnlineComentariosEncuestasUiStateConAvatares(
    val comentarios: List<ComentarioOnlineConVotos> = emptyList(),
    val encuestas: List<EncuestaOnlineConResultadosConAvatar> = emptyList()
)

// ---------- EVENTOS: MODELOS DE UI ----------
data class GolEvento(
    val equipoUid: String,
    val jugador: String,
    val minuto: Int?,
    val asistente: String?
)

data class JugadorOption(
    val uid: String?,     // null -> nombre manual
    val nombre: String,
    val esManual: Boolean
)

data class PartidoEventosUiState(
    val isLoading: Boolean = true,
    val guardando: Boolean = false,
    val error: String? = null,

    val reloadToken: Int = 0,

    val equipoAUid: String? = null,
    val equipoBUid: String? = null,
    val nombreEquipoA: String? = null,
    val nombreEquipoB: String? = null,

    val jugadoresEquipoA: List<JugadorOption> = emptyList(),
    val jugadoresEquipoB: List<JugadorOption> = emptyList(),

    val eventos: List<GolEvento> = emptyList(),
    val puedeEditar: Boolean = false
)

// ---------- VIEWMODEL ----------
class VisualizarPartidoOnlineViewModel(
    private val partidoUid: String,
    private val repo: PartidoFirebaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VisualizarPartidoOnlineUiState())
    val uiState: StateFlow<VisualizarPartidoOnlineUiState> = _uiState

    private val _eliminado = MutableStateFlow(false)
    val eliminado: StateFlow<Boolean> = _eliminado

    private val _comentariosEncuestasState = MutableStateFlow(
        VisualizarPartidoOnlineComentariosEncuestasUiStateConAvatares()
    )
    val comentariosEncuestasState: StateFlow<VisualizarPartidoOnlineComentariosEncuestasUiStateConAvatares> =
        _comentariosEncuestasState

    private val _partidoCreadorUid = MutableStateFlow<String?>(null)
    val partidoCreadorUid: StateFlow<String?> = _partidoCreadorUid

    // --- NUEVO: UI STATE PARA EVENTOS ---
    private val _uiStateEventos = MutableStateFlow(PartidoEventosUiState())
    val uiStateEventos: StateFlow<PartidoEventosUiState> = _uiStateEventos

    // ---------- LÓGICA EXISTENTE ----------
    fun cargarDatos(usuarioUid: String? = null) {
        viewModelScope.launch {
            val partido = repo.obtenerPartido(partidoUid)
            if (partido != null) {
                _partidoCreadorUid.value = partido.creadorUid

                val equipoA = partido.equipoAId.let { repo.obtenerEquipo(it) }
                val equipoB = partido.equipoBId.let { repo.obtenerEquipo(it) }

                val nombresManualA = partido.nombresManualEquipoA ?: emptyList()
                val nombresManualB = partido.nombresManualEquipoB ?: emptyList()
                val jugadoresA = obtenerNombresPorUid(partido.jugadoresEquipoA)
                val jugadoresB = obtenerNombresPorUid(partido.jugadoresEquipoB)

                val listaFinalA = jugadoresA + nombresManualA
                val listaFinalB = jugadoresB + nombresManualB

                _uiState.value = VisualizarPartidoOnlineUiState(
                    nombreEquipoA = equipoA?.nombre ?: "Equipo A",
                    nombreEquipoB = equipoB?.nombre ?: "Equipo B",
                    jugadoresEquipoA = listaFinalA,
                    jugadoresEquipoB = listaFinalB,
                    estado = calcularMinutoYParte(
                        partido.fecha,
                        partido.horaInicio,
                        partido.numeroPartes,
                        partido.tiempoPorParte,
                        partido.tiempoDescanso
                    ).estadoVisible,
                    minutoActual = "",
                    parteActual = 0,
                    partesTotales = partido.numeroPartes,
                    golesEquipoA = partido.golesEquipoA,
                    golesEquipoB = partido.golesEquipoB
                )
            }
            cargarComentariosEncuestas(usuarioUid)
        }
    }

    fun cargarComentariosEncuestas(usuarioUid: String? = null) {
        viewModelScope.launch {
            val comentarios = repo.obtenerComentarios(partidoUid)
            val db = FirebaseFirestore.getInstance()
            val comentariosConVotos = comentarios.map { comentario ->
                val likes = repo.obtenerVotosComentario(comentario.uid, 1)
                val dislikes = repo.obtenerVotosComentario(comentario.uid, -1)
                val miVoto = usuarioUid?.let { repo.obtenerVotoUsuarioComentario(comentario.uid, it) }
                var avatar: Int? = null
                if (comentario.usuarioUid.isNotBlank()) {
                    val snap = db.collection("usuarios").document(comentario.usuarioUid).get().await()
                    avatar = snap.getLong("avatar")?.toInt()
                }
                ComentarioOnlineConVotos(comentario, avatar, likes, dislikes, miVoto)
            }
            val encuestas = repo.obtenerEncuestas(partidoUid)
            val encuestasConResultados = encuestas.map { encuesta ->
                var avatar: Int? = null
                if (encuesta.creadorUid.isNotBlank()) {
                    val snap = db.collection("usuarios").document(encuesta.creadorUid).get().await()
                    avatar = snap.getLong("avatar")?.toInt()
                }
                val votosPorOpcion = repo.obtenerVotosPorOpcionEncuesta(encuesta.uid, encuesta.opciones.size)
                val votosList = MutableList(encuesta.opciones.size) { 0 }
                votosPorOpcion.forEach { votosList[it.opcionIndex] = it.votos }
                EncuestaOnlineConResultadosConAvatar(encuesta, votosList, avatar)
            }
            _comentariosEncuestasState.value =
                VisualizarPartidoOnlineComentariosEncuestasUiStateConAvatares(
                    comentarios = comentariosConVotos,
                    encuestas = encuestasConResultados
                )
        }
    }

    fun agregarComentario(usuarioNombre: String, texto: String, usuarioUid: String? = null) {
        viewModelScope.launch {
            var nombreFinal = usuarioNombre
            val uid = usuarioUid ?: ""
            var avatar: Int? = null

            if (uid.isNotBlank()) {
                try {
                    val db = FirebaseFirestore.getInstance()
                    val snapUsuario = db.collection("usuarios").document(uid).get().await()
                    nombreFinal = snapUsuario.getString("nombreUsuario") ?: "Usuario"
                    avatar = snapUsuario.getLong("avatar")?.toInt()
                } catch (_: Exception) {
                    nombreFinal = "Usuario"
                    avatar = null
                }
            }

            val fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            val comentario = ComentarioFirebase(
                partidoUid = partidoUid,
                usuarioUid = uid,
                usuarioNombre = nombreFinal,
                texto = texto,
                fechaHora = fechaHora,
            )
            repo.agregarComentario(comentario)
            cargarComentariosEncuestas(usuarioUid)
        }
    }

    fun votarComentario(comentarioUid: String, usuarioUid: String, tipo: Int) {
        viewModelScope.launch {
            repo.votarComentario(comentarioUid, usuarioUid, tipo)
            cargarComentariosEncuestas(usuarioUid)
        }
    }

    fun eliminarComentario(comentarioUid: String, usuarioUid: String) {
        viewModelScope.launch {
            repo.eliminarComentarioSiAutorizado(comentarioUid, usuarioUid)
            cargarComentariosEncuestas(usuarioUid)
        }
    }

    fun agregarEncuesta(
        pregunta: String,
        opciones: List<String>,
        usuarioUid: String? = null
    ) {
        if (opciones.isEmpty() || opciones.size > 5) return
        viewModelScope.launch {
            var creadorNombre = "Anónimo"
            val uid = usuarioUid ?: ""
            if (uid.isNotBlank()) {
                try {
                    val db = FirebaseFirestore.getInstance()
                    val snapUsuario = db.collection("usuarios").document(uid).get().await()
                    creadorNombre = snapUsuario.getString("nombreUsuario") ?: "Usuario"
                } catch (_: Exception) {
                    creadorNombre = "Usuario"
                }
            }
            val encuesta = hashMapOf(
                "partidoUid" to partidoUid,
                "pregunta" to pregunta,
                "opciones" to opciones,
                "creadorNombre" to creadorNombre,
                "creadorUid" to uid
            )
            val db = FirebaseFirestore.getInstance()
            db.collection("encuestas")
                .add(encuesta)
                .addOnSuccessListener { cargarComentariosEncuestas(usuarioUid) }
        }
    }

    suspend fun getVotoUsuarioEncuesta(encuestaUid: String, usuarioUid: String): Int? {
        return repo.obtenerVotoUsuarioEncuesta(encuestaUid, usuarioUid)
    }

    fun votarUnicoEnEncuesta(encuestaUid: String, opcionIndex: Int, usuarioUid: String) {
        viewModelScope.launch {
            repo.votarEncuestaUnico(encuestaUid, opcionIndex, usuarioUid)
            cargarComentariosEncuestas(usuarioUid)
        }
    }

    fun eliminarPartido() {
        viewModelScope.launch {
            repo.borrarPartido(partidoUid)
            _eliminado.value = true
        }
    }

    private data class InfoMinutoParte(
        val estadoVisible: String,
        val minutoVisible: String,
        val parteActual: Int
    )

    private fun calcularMinutoYParte(
        fecha: String,
        horaInicio: String,
        partes: Int,
        minPorParte: Int,
        minDescanso: Int
    ): InfoMinutoParte {
        try {
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
            val inicio = LocalDateTime.parse("$fecha $horaInicio", formatter)
            val ahora = LocalDateTime.now()
            val duracionTotalMin = (partes * minPorParte) + ((partes - 1) * minDescanso)
            val fin = inicio.plusMinutes(duracionTotalMin.toLong())

            if (ahora.isBefore(inicio)) return InfoMinutoParte("Previa", "-", 0)
            if (ahora.isAfter(fin)) return InfoMinutoParte("Finalizado", "-", partes)

            var t = 0L
            for (parte in 1..partes) {
                val iniParte = inicio.plusMinutes(t)
                val finParte = iniParte.plusMinutes(minPorParte.toLong())
                if (ahora.isBefore(finParte)) {
                    val minuto = (java.time.Duration.between(iniParte, ahora).toMinutes() + 1).coerceAtLeast(1)
                    return InfoMinutoParte("Jugando", "Parte $parte | Minuto $minuto", parte)
                }
                t += minPorParte
                if (parte != partes) {
                    val finDescanso = finParte.plusMinutes(minDescanso.toLong())
                    if (ahora.isBefore(finDescanso)) {
                        return InfoMinutoParte("Descanso", "Descanso entre parte $parte y ${parte + 1}", parte)
                    }
                    t += minDescanso
                }
            }
            return InfoMinutoParte("Finalizado", "-", partes)
        } catch (e: Exception) {
            return InfoMinutoParte("-", "-", 0)
        }
    }

    fun dejarDeVerPartido(usuarioUid: String, onFinish: () -> Unit) {
        viewModelScope.launch {
            repo.quitarUsuarioDeAcceso(partidoUid, usuarioUid)
            onFinish()
        }
    }

    // ---------- NUEVO BLOQUE: EVENTOS 100% EN EL VM ----------

    fun recargar() {
        _uiStateEventos.value = _uiStateEventos.value.copy(
            reloadToken = _uiStateEventos.value.reloadToken + 1
        )
        // disparar carga real
        cargarEventosYEquipos(partidoUid)
    }

    fun cargarEventosYEquipos(partidoUid: String) {
        viewModelScope.launch {
            try {
                _uiStateEventos.value = _uiStateEventos.value.copy(isLoading = true, error = null)

                val db = FirebaseFirestore.getInstance()
                val snapPartido = db.collection("partidos").document(partidoUid).get().await()
                val equipoAUid = snapPartido.getString("equipoAId")
                val equipoBUid = snapPartido.getString("equipoBId")
                val creadorUid = snapPartido.getString("creadorUid") ?: ""
                val administradores = (snapPartido.get("administradores") as? List<*>)?.filterIsInstance<String>() ?: emptyList()

                val currentUid = FirebaseAuth.getInstance().currentUser?.uid
                val puedeEditar = currentUid != null && (currentUid == creadorUid || administradores.contains(currentUid))

                val equipoA = equipoAUid?.let {
                    db.collection("equipos").document(it).get().await().toObject(EquipoFirebase::class.java)
                }
                val equipoB = equipoBUid?.let {
                    db.collection("equipos").document(it).get().await().toObject(EquipoFirebase::class.java)
                }

                // Jugadores para diálogo: roster online + nombres manuales guardados en el partido
                val jugadoresEquipoA = cargarJugadoresPorEquipoForVm(db, partidoUid, equipoAUid, snapPartido)
                val jugadoresEquipoB = cargarJugadoresPorEquipoForVm(db, partidoUid, equipoBUid, snapPartido)

                // Cargar goleadores del partido
                val golesDocs = db.collection("goleadores")
                    .whereEqualTo("partidoUid", partidoUid)
                    .get().await()
                    .documents

                val goles = golesDocs.mapNotNull { it.toObject(GoleadorFirebase::class.java)?.copy(uid = it.id) }

                // Resolver nombres por UID (jugador o usuario)
                val jugadorUids = goles.mapNotNull { it.jugadorUid?.takeIf { u -> u.isNotBlank() } } +
                        goles.mapNotNull { it.asistenciaJugadorUid?.takeIf { u -> u.isNotBlank() } }
                val nombresMap = mutableMapOf<String, String>()
                for (uid in jugadorUids.distinct()) {
                    val jugSnap = db.collection("jugadores").document(uid).get().await()
                    val nombreJugador = jugSnap.getString("nombre")
                    if (!nombreJugador.isNullOrBlank()) {
                        nombresMap[uid] = nombreJugador
                    } else {
                        val userSnap = db.collection("usuarios").document(uid).get().await()
                        userSnap.getString("nombreUsuario")?.let { nombresMap[uid] = it }
                    }
                }

// golesDocs ya lo tienes; lo mapeamos por id para leer claves legacy
                val docsById = golesDocs.associateBy { it.id }

                val eventos = goles
                    .sortedBy { it.minuto ?: Int.MIN_VALUE }
                    .map { gol ->
                        val doc = docsById[gol.uid]

                        // Jugador: preferir UID -> nombre por mapa; si no, leer ambos nombres manuales
                        val jugadorNombreManualLegacy = doc?.getString("jugadorManual") // legacy
                        val jugadorNombreManualNuevo = gol.jugadorNombreManual // data class
                        val nombreJugador = when {
                            !gol.jugadorUid.isNullOrBlank() ->
                                nombresMap[gol.jugadorUid] ?: jugadorNombreManualNuevo ?: jugadorNombreManualLegacy ?: ""
                            !jugadorNombreManualNuevo.isNullOrBlank() -> jugadorNombreManualNuevo!!
                            !jugadorNombreManualLegacy.isNullOrBlank() -> jugadorNombreManualLegacy!!
                            else -> ""
                        }

                        // Asistente: igual lógica
                        val asistenciaManualLegacy = doc?.getString("asistenciaManual")
                        val asistenciaManualNueva = gol.asistenciaNombreManual
                        val nombreAsistente = when {
                            !gol.asistenciaJugadorUid.isNullOrBlank() ->
                                nombresMap[gol.asistenciaJugadorUid] ?: asistenciaManualNueva ?: asistenciaManualLegacy
                            !asistenciaManualNueva.isNullOrBlank() -> asistenciaManualNueva
                            !asistenciaManualLegacy.isNullOrBlank() -> asistenciaManualLegacy
                            else -> null
                        }

                        GolEvento(
                            equipoUid = gol.equipoUid,
                            jugador = nombreJugador.ifBlank { "Desconocido" },
                            minuto = gol.minuto,
                            asistente = nombreAsistente
                        )
                    }


                _uiStateEventos.value = _uiStateEventos.value.copy(
                    isLoading = false,
                    error = null,
                    equipoAUid = equipoAUid,
                    equipoBUid = equipoBUid,
                    nombreEquipoA = equipoA?.nombre ?: "Equipo A",
                    nombreEquipoB = equipoB?.nombre ?: "Equipo B",
                    jugadoresEquipoA = jugadoresEquipoA,
                    jugadoresEquipoB = jugadoresEquipoB,
                    eventos = eventos,
                    puedeEditar = puedeEditar
                )
            } catch (e: Exception) {
                _uiStateEventos.value = _uiStateEventos.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error cargando eventos"
                )
            }
        }
    }

    private suspend fun cargarJugadoresPorEquipoForVm(
        db: FirebaseFirestore,
        partidoUid: String,
        equipoId: String?,
        snapPartido: com.google.firebase.firestore.DocumentSnapshot
    ): List<JugadorOption> {
        if (equipoId.isNullOrBlank()) return emptyList()

        val jugadoresA = (snapPartido.get("jugadoresEquipoA") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
        val jugadoresB = (snapPartido.get("jugadoresEquipoB") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
        val manualA = (snapPartido.get("nombresManualEquipoA") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
        val manualB = (snapPartido.get("nombresManualEquipoB") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()

        val uids = if (equipoId == snapPartido.getString("equipoAId")) jugadoresA else jugadoresB
        val manual = if (equipoId == snapPartido.getString("equipoAId")) manualA else manualB

        val nombresPorUid = mutableMapOf<String, String>()
        for (uid in uids) {
            val jugSnap = db.collection("jugadores").document(uid).get().await()
            val nombre = jugSnap.getString("nombre")
            if (!nombre.isNullOrBlank()) {
                nombresPorUid[uid] = nombre
            } else {
                val userSnap = db.collection("usuarios").document(uid).get().await()
                userSnap.getString("nombreUsuario")?.let { nombresPorUid[uid] = it }
            }
        }

        val online = uids.map { uid -> JugadorOption(uid = uid, nombre = nombresPorUid[uid] ?: uid, esManual = false) }
        val manualOpts = manual.map { nombre -> JugadorOption(uid = null, nombre = nombre, esManual = true) }
        return online + manualOpts
    }

    fun agregarEvento(
        partidoUid: String,
        equipoUid: String,
        jugador: JugadorOption,
        minuto: Int?,
        asistente: JugadorOption?,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _uiStateEventos.value = _uiStateEventos.value.copy(guardando = true)

                val db = FirebaseFirestore.getInstance()
                val doc = db.collection("goleadores").document()

                val data = hashMapOf<String, Any?>(
                    "uid" to doc.id,
                    "partidoUid" to partidoUid,
                    "equipoUid" to equipoUid,
                    "minuto" to minuto,
                    "timestamp" to FieldValue.serverTimestamp()
                )

                // jugador
                if (jugador.uid.isNullOrBlank()) {
                    data["jugadorUid"] = ""
                    data["jugadorManual"] = jugador.nombre
                } else {
                    data["jugadorUid"] = jugador.uid
                    data["jugadorManual"] = ""
                }

                // asistencia
                if (asistente == null) {
                    data["asistenciaJugadorUid"] = ""
                    data["asistenciaManual"] = ""
                } else if (asistente.uid.isNullOrBlank()) {
                    data["asistenciaJugadorUid"] = ""
                    data["asistenciaManual"] = asistente.nombre
                } else {
                    data["asistenciaJugadorUid"] = asistente.uid
                    data["asistenciaManual"] = ""
                }

                doc.set(data).await()

                // Actualizar marcador del partido
                actualizarMarcadorPartido(partidoUid, equipoUid)

                _uiStateEventos.value = _uiStateEventos.value.copy(guardando = false)
                onSuccess()
                // recargar lista
                cargarEventosYEquipos(partidoUid)
            } catch (e: Exception) {
                _uiStateEventos.value = _uiStateEventos.value.copy(
                    guardando = false,
                    error = e.message ?: "Error guardando evento"
                )
            }
        }
    }

    private suspend fun actualizarMarcadorPartido(partidoUid: String, equipoUid: String) {
        val db = FirebaseFirestore.getInstance()
        val golesCount = db.collection("goleadores")
            .whereEqualTo("partidoUid", partidoUid)
            .whereEqualTo("equipoUid", equipoUid)
            .get().await().size()

        val equipoAId = obtenerEquipoAId(partidoUid)
        val campo = if (equipoUid == equipoAId) "golesEquipoA" else "golesEquipoB"
        db.collection("partidos").document(partidoUid).update(campo, golesCount).await()
    }

    private suspend fun obtenerEquipoAId(partidoUid: String): String {
        val db = FirebaseFirestore.getInstance()
        val snap = db.collection("partidos").document(partidoUid).get().await()
        return snap.getString("equipoAId") ?: ""
    }

    // ---------- UTILIDADES EXISTENTES ----------
    private suspend fun obtenerNombresPorUid(uids: List<String>): List<String> {
        if (uids.isEmpty()) return emptyList()
        val db = FirebaseFirestore.getInstance()
        val nombres = mutableListOf<String>()
        for (uid in uids) {
            if (uid.isBlank()) continue
            val snapJugador = db.collection("jugadores").document(uid).get().await()
            val nombreJugador = snapJugador.getString("nombre")
            if (!nombreJugador.isNullOrBlank()) {
                nombres.add(nombreJugador)
                continue
            }
            val snapUsuario = db.collection("usuarios").document(uid).get().await()
            val nombreUsuario = snapUsuario.getString("nombreUsuario")
            if (!nombreUsuario.isNullOrBlank()) {
                nombres.add(nombreUsuario)
                continue
            }
            nombres.add("Desconocido")
        }
        return nombres
    }
}
