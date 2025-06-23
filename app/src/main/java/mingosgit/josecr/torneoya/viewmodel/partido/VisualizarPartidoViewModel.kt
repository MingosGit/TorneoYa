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



class VisualizarPartidoViewModel(
    private val partidoId: Long,
    private val partidoRepository: PartidoRepository,
    private val equipoRepository: EquipoRepository,
    private val comentarioRepository: ComentarioRepository,
    private val encuestaRepository: EncuestaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VisualizarPartidoUiState())
    val uiState: StateFlow<VisualizarPartidoUiState> = _uiState

    private val _eliminado = MutableStateFlow(false)
    val eliminado: StateFlow<Boolean> = _eliminado

    private val _comentariosEncuestasState = MutableStateFlow(VisualizarPartidoComentariosEncuestasUiState())
    val comentariosEncuestasState: StateFlow<VisualizarPartidoComentariosEncuestasUiState> = _comentariosEncuestasState

    fun cargarDatos() {
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
            cargarComentariosEncuestas()
        }
    }


    suspend fun getVotoUsuarioEncuesta(encuestaId: Long, usuarioId: Long): Int? {
        return encuestaRepository.getVotoUsuario(encuestaId, usuarioId)
    }

    fun votarUnicoEnEncuesta(encuestaId: Long, opcionIndex: Int, usuarioId: Long) {
        viewModelScope.launch {
            encuestaRepository.votarUnico(encuestaId, opcionIndex, usuarioId)
            cargarComentariosEncuestas()
        }
    }

    fun cargarComentariosEncuestas() {
        viewModelScope.launch {
            val comentarios = comentarioRepository.obtenerComentarios(partidoId)
            val encuestas = encuestaRepository.obtenerEncuestas(partidoId)
            val encuestasConResultados = encuestas.map { encuesta ->
                val votosPorOpcion = encuestaRepository.votosPorOpcion(encuesta.id)
                val opciones = encuesta.opciones.split("|")
                val votosList = MutableList(opciones.size) { 0 }
                votosPorOpcion.forEach { votosList[it.opcionIndex] = it.votos }
                EncuestaConResultados(encuesta, votosList)
            }
            _comentariosEncuestasState.value = VisualizarPartidoComentariosEncuestasUiState(
                comentarios = comentarios,
                encuestas = encuestasConResultados
            )
        }
    }

    fun agregarComentario(usuarioNombre: String, texto: String) {
        viewModelScope.launch {
            val fechaHora = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            val comentario = ComentarioEntity(
                partidoId = partidoId,
                usuarioNombre = usuarioNombre,
                texto = texto,
                fechaHora = fechaHora
            )
            comentarioRepository.agregarComentario(comentario)
            cargarComentariosEncuestas()
        }
    }

    fun agregarEncuesta(pregunta: String, opciones: List<String>) {
        if (opciones.isEmpty() || opciones.size > 5) return
        viewModelScope.launch {
            val encuesta = EncuestaEntity(
                partidoId = partidoId,
                pregunta = pregunta,
                opciones = opciones.joinToString("|")
            )
            encuestaRepository.agregarEncuesta(encuesta)
            cargarComentariosEncuestas()
        }
    }

//    fun votarEnEncuesta(encuestaId: Long, opcionIndex: Int) {
//        viewModelScope.launch {
//            encuestaRepository.votar(encuestaId, opcionIndex)
//            cargarComentariosEncuestas()
//        }
//    }

    fun eliminarPartido() {
        viewModelScope.launch {
            val partido = partidoRepository.getPartidoById(partidoId)
            if (partido != null) {
                partidoRepository.deletePartido(partido)
                _eliminado.value = true
            }
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

class VisualizarPartidoViewModelFactory(
    private val partidoId: Long,
    private val partidoRepository: PartidoRepository,
    private val equipoRepository: EquipoRepository,
    private val comentarioRepository: ComentarioRepository,
    private val encuestaRepository: EncuestaRepository
) : ViewModelProvider.Factory {
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
