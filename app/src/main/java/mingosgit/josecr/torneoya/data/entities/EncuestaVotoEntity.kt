package mingosgit.josecr.torneoya.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "encuesta_voto")
data class EncuestaVotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val encuestaId: Long,
    val opcionIndex: Int,
    val usuarioId: Long
)
