package mingosgit.josecr.torneoya.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comentario")
data class ComentarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val partidoId: Long,
    val usuarioNombre: String,
    val texto: String,
    val fechaHora: String // Formato ISO: yyyy-MM-dd HH:mm:ss
)
