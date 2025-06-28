package mingosgit.josecr.torneoya.data.entities

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "equipo_predefinido_jugadorcrossref",
    primaryKeys = ["equipoPredefinidoId", "jugadorId"],
    indices = [Index("jugadorId")] // Para evitar la warning del índice
)
data class EquipoPredefinidoJugadorCrossRef(
    val equipoPredefinidoId: Long,
    val jugadorId: Long
)
