package mingosgit.josecr.torneoya.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "evento")
data class EventoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val partidoId: Long,
    val tipo: String, // "GOL", otros tipos futuros...
    val minuto: Int?,
    val equipoId: Long,
    val jugadorId: Long,
    val asistenteId: Long?, // null si no hay asistencia
    val fechaHora: String
)
