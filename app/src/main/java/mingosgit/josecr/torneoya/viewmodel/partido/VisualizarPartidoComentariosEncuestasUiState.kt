package mingosgit.josecr.torneoya.viewmodel.partido

import mingosgit.josecr.torneoya.data.entities.ComentarioEntity
import mingosgit.josecr.torneoya.data.entities.EncuestaEntity

data class VisualizarPartidoComentariosEncuestasUiState(
    val comentarios: List<ComentarioEntity> = emptyList(),
    val encuestas: List<EncuestaConResultados> = emptyList()
)

data class EncuestaConResultados(
    val encuesta: EncuestaEntity,
    val votos: List<Int>
)
