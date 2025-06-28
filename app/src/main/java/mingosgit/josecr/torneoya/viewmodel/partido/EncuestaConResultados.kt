package mingosgit.josecr.torneoya.viewmodel.partido

import mingosgit.josecr.torneoya.data.entities.EncuestaEntity

data class EncuestaConResultados(
    val encuesta: EncuestaEntity,
    val votos: List<Int>
)
