package mingosgit.josecr.torneoya.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comentario_voto")
data class ComentarioVotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val comentarioId: Long,
    val usuarioId: Long,
    val tipo: Int // 1 = like, -1 = dislike
)
