package mingosgit.josecr.torneoya.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "partidos")
data class PartidoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val equipoLocalId: Long,
    val equipoVisitanteId: Long,
    val fecha: Long, // timestamp en millis
    val golesLocal: Int? = null,
    val golesVisitante: Int? = null,
    val torneoId: Long? = null // null si es partido suelto
)
