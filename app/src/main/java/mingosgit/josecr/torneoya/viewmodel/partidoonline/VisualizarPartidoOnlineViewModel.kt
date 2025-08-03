package mingosgit.josecr.torneoya.viewmodel.partidoonline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mingosgit.josecr.torneoya.data.entities.UsuarioFirebaseEntity
import mingosgit.josecr.torneoya.data.firebase.PartidoFirebaseRepository
import mingosgit.josecr.torneoya.data.firebase.PartidoFirebase
import mingosgit.josecr.torneoya.data.firebase.EquipoFirebase
import mingosgit.josecr.torneoya.data.firebase.ComentarioFirebase
import mingosgit.josecr.torneoya.data.firebase.EncuestaFirebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

data class ComentarioOnlineConVotos(
    val comentario: ComentarioFirebase,
    val likes: Int,
    val dislikes: Int,
    val miVoto: Int? // 1=like, -1=dislike, null=sin voto
)

data class EncuestaOnlineConResultados(
    val encuesta: EncuestaFirebase,
    val votos: List<Int>
)

data class VisualizarPartidoOnlineComentariosEncuestasUiState(
    val comentarios: List<ComentarioOnlineConVotos> = emptyList(),
    val encuestas: List<EncuestaOnlineConResultados> = emptyList()
)

class VisualizarPartidoOnlineViewModel(
    private val partidoUid: String,
    private val repo: PartidoFirebaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VisualizarPartidoOnlineUiState())
    val uiState: StateFlow<VisualizarPartidoOnlineUiState> = _uiState

    private val _eliminado = MutableStateFlow(false)
    val eliminado: StateFlow<Boolean> = _eliminado

    private val _comentariosEncuestasState = MutableStateFlow(VisualizarPartidoOnlineComentariosEncuestasUiState())
    val comentariosEncuestasState: StateFlow<VisualizarPartidoOnlineComentariosEncuestasUiState> = _comentariosEncuestasState

    fun cargarDatos(usuarioUid: String? = null) {
        viewModelScope.launch {
            val partido = repo.obtenerPartido(partidoUid)
            if (partido != null) {
                val equipoA = partido.equipoAId.let { repo.obtenerEquipo(it) }
                val equipoB = partido.equipoBId.let { repo.obtenerEquipo(it) }

                val nombresManualA = partido.nombresManualEquipoA ?: emptyList()
                val nombresManualB = partido.nombresManualEquipoB ?: emptyList()

                // *** ESTO AHORA BUSCA EN AMBAS COLECCIONES ***
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





    fun dejarDeVerPartido(usuarioUid: String, onFinish: () -> Unit) {
        viewModelScope.launch {
            repo.quitarUsuarioDeAcceso(partidoUid, usuarioUid)
            onFinish()
        }
    }


    fun cargarComentariosEncuestas(usuarioUid: String? = null) {
        viewModelScope.launch {
            val comentarios = repo.obtenerComentarios(partidoUid)
            val comentariosConVotos = comentarios.map { comentario ->
                val likes = repo.obtenerVotosComentario(comentario.uid, 1)
                val dislikes = repo.obtenerVotosComentario(comentario.uid, -1)
                val miVoto = usuarioUid?.let { repo.obtenerVotoUsuarioComentario(comentario.uid, it) }
                ComentarioOnlineConVotos(
                    comentario = comentario,
                    likes = likes,
                    dislikes = dislikes,
                    miVoto = miVoto
                )
            }
            val encuestas = repo.obtenerEncuestas(partidoUid)
            val encuestasConResultados = encuestas.map { encuesta ->
                val votosPorOpcion = repo.obtenerVotosPorOpcionEncuesta(encuesta.uid, encuesta.opciones.size)
                val votosList = MutableList(encuesta.opciones.size) { 0 }
                votosPorOpcion.forEach { votosList[it.opcionIndex] = it.votos }
                EncuestaOnlineConResultados(encuesta, votosList)
            }
            _comentariosEncuestasState.value = VisualizarPartidoOnlineComentariosEncuestasUiState(
                comentarios = comentariosConVotos,
                encuestas = encuestasConResultados
            )
        }
    }

    fun agregarComentario(usuarioNombre: String, texto: String, usuarioUid: String? = null) {
        viewModelScope.launch {
            var nombreFinal = usuarioNombre
            val uid = usuarioUid ?: ""

            // Si el nombre está vacío o es "Tú", intenta buscar el nombre real del usuario
            if (nombreFinal.isBlank() || nombreFinal.trim().lowercase() == "tú") {
                try {
                    if (uid.isNotBlank()) {
                        val db = FirebaseFirestore.getInstance()
                        val snapUsuario = db.collection("usuarios").document(uid).get().await()
                        nombreFinal = snapUsuario.getString("nombreUsuario") ?: "Usuario"
                    }
                } catch (_: Exception) {
                    nombreFinal = "Usuario"
                }
            }

            val fechaHora = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            val comentario = ComentarioFirebase(
                partidoUid = partidoUid,
                usuarioUid = uid,
                usuarioNombre = nombreFinal,
                texto = texto,
                fechaHora = fechaHora
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

    fun agregarEncuesta(pregunta: String, opciones: List<String>, usuarioUid: String? = null) {
        if (opciones.isEmpty() || opciones.size > 5) return
        viewModelScope.launch {
            var creadorNombre = "Anónimo"
            val uid = usuarioUid ?: ""
            if (uid.isNotBlank()) {
                try {
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    val snapUsuario = db.collection("usuarios").document(uid).get().await()
                    creadorNombre = snapUsuario.getString("nombreUsuario") ?: "Usuario"
                } catch (_: Exception) { creadorNombre = "Usuario" }
            }
            val encuesta = EncuestaFirebase(
                partidoUid = partidoUid,
                pregunta = pregunta,
                opciones = opciones,
                creadorNombre = creadorNombre
            )
            repo.agregarEncuesta(encuesta)
            cargarComentariosEncuestas(usuarioUid)
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
    private suspend fun obtenerNombresPorUid(uids: List<String>): List<String> {
        if (uids.isEmpty()) return emptyList()
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val nombres = mutableListOf<String>()
        for (uid in uids) {
            if (uid.isBlank()) continue

            // PRIMERO busca en 'jugadores'
            val snapJugador = db.collection("jugadores").document(uid).get().await()
            val nombreJugador = snapJugador.getString("nombre")
            if (!nombreJugador.isNullOrBlank()) {
                nombres.add(nombreJugador)
                continue
            }

            // Si no está, busca en 'usuarios'
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
