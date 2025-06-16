package mingosgit.josecr.torneoya.data.entities

import androidx.room.Entity

@Entity(
    tableName = "partido_equipo_jugador",
    primaryKeys = ["partidoId", "jugadorId"]
)
data class PartidoEquipoJugadorEntity(
    val partidoId: Long,
    val equipo: String, // "A" o "B"
    val jugadorId: Long
)
