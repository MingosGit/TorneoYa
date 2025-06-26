package mingosgit.josecr.torneoya.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goleador")
data class GoleadorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val partidoId: Long,
    val equipoId: Long,
    val jugadorId: Long,
    val minuto: Int?,
    val asistenciaJugadorId: Long? // null si no hay asistencia
)
