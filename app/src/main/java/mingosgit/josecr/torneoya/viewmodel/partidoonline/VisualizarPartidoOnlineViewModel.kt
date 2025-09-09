package mingosgit.josecr.torneoya.viewmodel.partidoonline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mingosgit.josecr.torneoya.data.firebase.PartidoFirebaseRepository
import mingosgit.josecr.torneoya.data.firebase.ComentarioFirebase
import mingosgit.josecr.torneoya.data.firebase.EncuestaFirebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Estado de UI: nombres, listas de jugadores, marcador y estado temporal del partido.
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

// Comentario enriquecido con avatar y conteo de votos (+ mi voto).
data class ComentarioOnlineConVotos(
    val comentario: ComentarioFirebase,
    val avatar: Int?,
    val likes: Int,
    val dislikes: Int,
    val miVoto: Int? // 1=like, -1=dislike, null=sin voto
)

// Encuesta con resultados por opción y avatar del creador.
data class EncuestaOnlineConResultadosConAvatar(
    val encuesta: EncuestaFirebase,
    val votos: List<Int>,
    val avatar: Int? // avatar del creador
)

// Estado de UI para lista de comentarios y encuestas (con avatares).
data class VisualizarPartidoOnlineComentariosEncuestasUiStateConAvatares(
    val comentarios: List<ComentarioOnlineConVotos> = emptyList(),
    val encuestas: List<EncuestaOnlineConResultadosConAvatar> = emptyList()
)

class VisualizarPartidoOnlineViewModel(
    private val partidoUid: String,
    private val repo: PartidoFirebaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VisualizarPartidoOnlineUiState())
    val uiState: StateFlow<VisualizarPartidoOnlineUiState> = _uiState

    private val _eliminado = MutableStateFlow(false)
    val eliminado: StateFlow<Boolean> = _eliminado

    private val _comentariosEncuestasState = MutableStateFlow(VisualizarPartidoOnlineComentariosEncuestasUiStateConAvatares())
    val comentariosEncuestasState: StateFlow<VisualizarPartidoOnlineComentariosEncuestasUiStateConAvatares> = _comentariosEncuestasState

    private val _partidoCreadorUid = MutableStateFlow<String?>(null)
    val partidoCreadorUid: StateFlow<String?> = _partidoCreadorUid

    // Carga datos del partido (nombres, jugadores, marcador y estado temporal) y después comentarios/encuestas.
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

    // Carga comentarios (con votos y avatar) y encuestas (con resultados y avatar) del partido.
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
                ComentarioOnlineConVotos(
                    comentario = comentario,
                    avatar = avatar,
                    likes = likes,
                    dislikes = dislikes,
                    miVoto = miVoto
                )
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
            _comentariosEncuestasState.value = VisualizarPartidoOnlineComentariosEncuestasUiStateConAvatares(
                comentarios = comentariosConVotos,
                encuestas = encuestasConResultados
            )
        }
    }

    // Añade un comentario (resolviendo nombre/avatar si hay usuario) y refresca la lista.
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

            val fechaHora = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
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

    // Registra un voto (like/dislike) sobre un comentario y recarga.
    fun votarComentario(comentarioUid: String, usuarioUid: String, tipo: Int) {
        viewModelScope.launch {
            repo.votarComentario(comentarioUid, usuarioUid, tipo)
            cargarComentariosEncuestas(usuarioUid)
        }
    }

    // Elimina un comentario si el usuario está autorizado y recarga.
    fun eliminarComentario(comentarioUid: String, usuarioUid: String) {
        viewModelScope.launch {
            repo.eliminarComentarioSiAutorizado(comentarioUid, usuarioUid)
            cargarComentariosEncuestas(usuarioUid)
        }
    }

    // Crea una encuesta (máx 5 opciones), guarda en Firestore y actualiza la vista al completar.
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
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    val snapUsuario = db.collection("usuarios").document(uid).get().await()
                    creadorNombre = snapUsuario.getString("nombreUsuario") ?: "Usuario"
                } catch (_: Exception) {
                    creadorNombre = "Usuario"
                }
            }
            // Construye el mapa y deja que Firestore asigne UID automáticamente con .add()
            val encuesta = hashMapOf(
                "partidoUid" to partidoUid,
                "pregunta" to pregunta,
                "opciones" to opciones,
                "creadorNombre" to creadorNombre,
                "creadorUid" to uid
            )
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("encuestas")
                .add(encuesta)
                .addOnSuccessListener {
                    cargarComentariosEncuestas(usuarioUid)
                }
                .addOnFailureListener {
                    // Aquí se podría registrar un log
                }
        }
    }

    // Devuelve el voto del usuario en una encuesta (si existe).
    suspend fun getVotoUsuarioEncuesta(encuestaUid: String, usuarioUid: String): Int? {
        return repo.obtenerVotoUsuarioEncuesta(encuestaUid, usuarioUid)
    }

    // Voto único para una opción de encuesta y recarga resultados.
    fun votarUnicoEnEncuesta(encuestaUid: String, opcionIndex: Int, usuarioUid: String) {
        viewModelScope.launch {
            repo.votarEncuestaUnico(encuestaUid, opcionIndex, usuarioUid)
            cargarComentariosEncuestas(usuarioUid)
        }
    }

    // Elimina el partido y marca el estado de eliminado.
    fun eliminarPartido() {
        viewModelScope.launch {
            repo.borrarPartido(partidoUid)
            _eliminado.value = true
        }
    }

    // DTO interno con estado visible, minuto y parte actual calculados.
    private data class InfoMinutoParte(
        val estadoVisible: String,
        val minutoVisible: String,
        val parteActual: Int
    )

    // Calcula estado/minuto/parte en función de fecha/hora, número de partes y descansos.
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

            if (ahora.isBefore(inicio)) {
                return InfoMinutoParte("Previa", "-", 0)
            }
            if (ahora.isAfter(fin)) {
                return InfoMinutoParte("Finalizado", "-", partes)
            }

            var t = 0L
            for (parte in 1..partes) {
                val iniParte = inicio.plusMinutes(t)
                val finParte = iniParte.plusMinutes(minPorParte.toLong())
                if (ahora.isBefore(finParte)) {
                    val minuto = (java.time.Duration.between(iniParte, ahora).toMinutes() + 1).coerceAtLeast(1)
                    return InfoMinutoParte(
                        "Jugando",
                        "Parte $parte | Minuto $minuto",
                        parte
                    )
                }
                t += minPorParte.toLong()
                if (parte != partes) {
                    val iniDescanso = finParte
                    val finDescanso = iniDescanso.plusMinutes(minDescanso.toLong())
                    if (ahora.isBefore(finDescanso)) {
                        return InfoMinutoParte("Descanso", "Descanso entre parte $parte y ${parte + 1}", parte)
                    }
                    t += minDescanso.toLong()
                }
            }
            return InfoMinutoParte("Finalizado", "-", partes)
        } catch (e: Exception) {
            return InfoMinutoParte("-", "-", 0)
        }
    }

    // Quita al usuario de la lista de acceso al partido y ejecuta callback al terminar.
    fun dejarDeVerPartido(usuarioUid: String, onFinish: () -> Unit) {
        viewModelScope.launch {
            repo.quitarUsuarioDeAcceso(partidoUid, usuarioUid)
            onFinish()
        }
    }

    // Busca nombres por uid: primero en "jugadores", si no existe prueba en "usuarios" y si no, pone "Desconocido".
    private suspend fun obtenerNombresPorUid(uids: List<String>): List<String> {
        if (uids.isEmpty()) return emptyList()
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
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
