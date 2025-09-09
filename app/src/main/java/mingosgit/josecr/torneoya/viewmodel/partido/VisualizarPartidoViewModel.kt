package mingosgit.josecr.torneoya.viewmodel.partido

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.entities.PartidoEntity
import mingosgit.josecr.torneoya.data.entities.PartidoEstado
import mingosgit.josecr.torneoya.repository.PartidoRepository
import mingosgit.josecr.torneoya.repository.EquipoRepository
import mingosgit.josecr.torneoya.repository.ComentarioRepository
import mingosgit.josecr.torneoya.repository.EncuestaRepository
import mingosgit.josecr.torneoya.data.entities.ComentarioEntity
import mingosgit.josecr.torneoya.data.entities.EncuestaEntity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import mingosgit.josecr.torneoya.viewmodel.partido.VisualizarPartidoComentariosEncuestasUiState
import mingosgit.josecr.torneoya.viewmodel.partido.EncuestaConResultados

// Estado UI con nombres, plantillas, marcador y tiempo del partido
data class VisualizarPartidoUiState(
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

// ViewModel que orquesta datos del partido y su feed (comentarios/encuestas)
class VisualizarPartidoViewModel(
    private val partidoId: Long,
    private val partidoRepository: PartidoRepository,
    private val equipoRepository: EquipoRepository,
    private val comentarioRepository: ComentarioRepository,
    private val encuestaRepository: EncuestaRepository
) : ViewModel() {

    // Estado principal de la cabecera del partido
    private val _uiState = MutableStateFlow(VisualizarPartidoUiState())
    val uiState: StateFlow<VisualizarPartidoUiState> = _uiState

    // Flag de que el partido se ha eliminado
    private val _eliminado = MutableStateFlow(false)
    val eliminado: StateFlow<Boolean> = _eliminado

    // Estado combinado de comentarios y encuestas para la UI
    private val _comentariosEncuestasState = MutableStateFlow(VisualizarPartidoComentariosEncuestasUiState())
    val comentariosEncuestasState: StateFlow<VisualizarPartidoComentariosEncuestasUiState> = _comentariosEncuestasState

    // Carga datos del partido y luego el feed; opcionalmente personaliza con usuarioId
    fun cargarDatos(usuarioId: Long? = null) {
        viewModelScope.launch {
            val partido = partidoRepository.getPartidoById(partidoId)
            if (partido != null) {
                val equipoAId = partido.equipoAId
                val equipoBId = partido.equipoBId

                val nombreEquipoA = equipoRepository.getById(equipoAId)?.nombre ?: "Equipo A"
                val nombreEquipoB = equipoRepository.getById(equipoBId)?.nombre ?: "Equipo B"
                val jugadoresA = equipoRepository.getNombresJugadoresEquipoEnPartido(partidoId, equipoAId)
                val jugadoresB = equipoRepository.getNombresJugadoresEquipoEnPartido(partidoId, equipoBId)

                val tiempo = calcularMinutoYParte(
                    partido.fecha,
                    partido.horaInicio,
                    partido.numeroPartes,
                    partido.tiempoPorParte,
                    partido.tiempoDescanso
                )

                _uiState.value = VisualizarPartidoUiState(
                    nombreEquipoA = nombreEquipoA,
                    nombreEquipoB = nombreEquipoB,
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
            cargarComentariosEncuestas(usuarioId)
        }
    }

    // Obtiene el voto del usuario en una encuesta concreta
    suspend fun getVotoUsuarioEncuesta(encuestaId: Long, usuarioId: Long): Int? {
        return encuestaRepository.getVotoUsuario(encuestaId, usuarioId)
    }

    // Emite voto único en encuesta y refresca resultados
    fun votarUnicoEnEncuesta(encuestaId: Long, opcionIndex: Int, usuarioId: Long) {
        viewModelScope.launch {
            encuestaRepository.votarUnico(encuestaId, opcionIndex, usuarioId)
            cargarComentariosEncuestas(usuarioId)
        }
    }

    // Carga comentarios con votos y encuestas con recuentos; opcionalmente marca mi voto
    fun cargarComentariosEncuestas(usuarioId: Long? = null) {
        viewModelScope.launch {
            val comentarios = comentarioRepository.obtenerComentarios(partidoId)
            val comentariosConVotos = comentarios.map { comentario ->
                val likes = comentarioRepository.getLikes(comentario.id)
                val dislikes = comentarioRepository.getDislikes(comentario.id)
                val miVoto = usuarioId?.let { comentarioRepository.getVotoUsuario(comentario.id, it)?.tipo }
                ComentarioConVotos(
                    comentario = comentario,
                    likes = likes,
                    dislikes = dislikes,
                    miVoto = miVoto
                )
            }
            val encuestas = encuestaRepository.obtenerEncuestas(partidoId)
            val encuestasConResultados = encuestas.map { encuesta ->
                val votosPorOpcion = encuestaRepository.votosPorOpcion(encuesta.id)
                val opciones = encuesta.opciones.split("|")
                val votosList = MutableList(opciones.size) { 0 }
                votosPorOpcion.forEach { votosList[it.opcionIndex] = it.votos }
                EncuestaConResultados(encuesta, votosList)
            }
            _comentariosEncuestasState.value = VisualizarPartidoComentariosEncuestasUiState(
                comentarios = comentariosConVotos,
                encuestas = encuestasConResultados
            )
        }
    }

    // Inserta un nuevo comentario con timestamp y refresca el feed
    fun agregarComentario(usuarioNombre: String, texto: String, usuarioId: Long? = null) {
        viewModelScope.launch {
            val fechaHora = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            val comentario = ComentarioEntity(
                partidoId = partidoId,
                usuarioNombre = usuarioNombre,
                texto = texto,
                fechaHora = fechaHora
            )
            comentarioRepository.agregarComentario(comentario)
            cargarComentariosEncuestas(usuarioId)
        }
    }

    // Vota un comentario (like/dislike) y refresca recuentos
    fun votarComentario(comentarioId: Long, usuarioId: Long, tipo: Int) {
        viewModelScope.launch {
            comentarioRepository.votarComentario(comentarioId, usuarioId, tipo)
            cargarComentariosEncuestas(usuarioId)
        }
    }

    // Crea una encuesta (hasta 5 opciones) y refresca el feed
    fun agregarEncuesta(pregunta: String, opciones: List<String>, usuarioId: Long? = null) {
        if (opciones.isEmpty() || opciones.size > 5) return
        viewModelScope.launch {
            val encuesta = EncuestaEntity(
                partidoId = partidoId,
                pregunta = pregunta,
                opciones = opciones.joinToString("|")
            )
            encuestaRepository.agregarEncuesta(encuesta)
            cargarComentariosEncuestas(usuarioId)
        }
    }

    // Elimina el partido en repositorio y marca estado eliminado
    fun eliminarPartido() {
        viewModelScope.launch {
            val partido = partidoRepository.getPartidoById(partidoId)
            if (partido != null) {
                partidoRepository.deletePartido(partido)
                _eliminado.value = true
            }
        }
    }

    // DTO interno para mostrar estado y tiempo actual
    private data class InfoMinutoParte(
        val estadoVisible: String,
        val minutoVisible: String,
        val parteActual: Int
    )

    // Calcula en qué parte/minuto va el partido o si está en previa/descanso/finalizado
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

    // Carga inicial automática al crear el ViewModel
    init {
        cargarDatos()
    }
}

// Factory para crear el ViewModel con sus dependencias
class VisualizarPartidoViewModelFactory(
    private val partidoId: Long,
    private val partidoRepository: PartidoRepository,
    private val equipoRepository: EquipoRepository,
    private val comentarioRepository: ComentarioRepository,
    private val encuestaRepository: EncuestaRepository
) : ViewModelProvider.Factory {
    // Construye instancia del ViewModel cuando la solicita el provider
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VisualizarPartidoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VisualizarPartidoViewModel(
                partidoId = partidoId,
                partidoRepository = partidoRepository,
                equipoRepository = equipoRepository,
                comentarioRepository = comentarioRepository,
                encuestaRepository = encuestaRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
