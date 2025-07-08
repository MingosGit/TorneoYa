package mingosgit.josecr.torneoya.viewmodel.partidoonline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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
                val jugadoresAll = repo.obtenerJugadores()
                val jugadoresA = jugadoresAll
                    .filter { it.uid in partido.jugadoresUids && it.uid.isNotBlank() }
                    .filter { it.uid != partido.equipoBId }
                    .map { it.nombre }
                val jugadoresB = jugadoresAll
                    .filter { it.uid in partido.jugadoresUids && it.uid.isNotBlank() }
                    .filter { it.uid != partido.equipoAId }
                    .map { it.nombre }
                val tiempo = calcularMinutoYParte(
                    partido.fecha,
                    partido.horaInicio,
                    partido.numeroPartes,
                    partido.tiempoPorParte,
                    partido.tiempoDescanso
                )
                _uiState.value = VisualizarPartidoOnlineUiState(
                    nombreEquipoA = equipoA?.nombre ?: "Equipo A",
                    nombreEquipoB = equipoB?.nombre ?: "Equipo B",
                    jugadoresEquipoA = jugadoresA,
                    jugadoresEquipoB = jugadoresB,
                    estado = tiempo.estadoVisible,
                    minutoActual = tiempo.minutoVisible,
                    parteActual = tiempo.parteActual,
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
            val fechaHora = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            val comentario = ComentarioFirebase(
                partidoUid = partidoUid,
                usuarioUid = usuarioUid ?: "",
                usuarioNombre = usuarioNombre,
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
            val encuesta = EncuestaFirebase(
                partidoUid = partidoUid,
                pregunta = pregunta,
                opciones = opciones
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

    init {
        cargarDatos()
    }
}
