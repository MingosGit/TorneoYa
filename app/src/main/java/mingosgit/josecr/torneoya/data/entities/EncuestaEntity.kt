package mingosgit.josecr.torneoya.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "encuesta")
data class EncuestaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val partidoId: Long,
    val pregunta: String,
    val opciones: String // Opciones separadas por "|", máx 5
)
