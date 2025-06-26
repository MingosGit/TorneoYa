package mingosgit.josecr.torneoya.data.entities

data class EstadisticasJugador(
    val jugadorId: Long,
    val nombre: String,
    val goles: Int,
    val asistencias: Int,
    val partidosJugados: Int
)
