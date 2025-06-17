package mingosgit.josecr.torneoya.data.entities

import androidx.room.Entity

@Entity(
    tableName = "partido_equipo_jugador",
    primaryKeys = ["partidoId", "equipoId", "jugadorId"]
)
data class PartidoEquipoJugadorEntity(
    val partidoId: Long,
    val equipoId: Long,
    val jugadorId: Long
)
