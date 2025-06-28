package mingosgit.josecr.torneoya.viewmodel.partido

import mingosgit.josecr.torneoya.data.entities.ComentarioEntity

data class ComentarioConVotos(
    val comentario: ComentarioEntity,
    val likes: Int,
    val dislikes: Int,
    val miVoto: Int? // 1=like, -1=dislike, null=sin voto
)

data class VisualizarPartidoComentariosEncuestasUiState(
    val comentarios: List<ComentarioConVotos> = emptyList(),
    val encuestas: List<EncuestaConResultados> = emptyList()
)
