package mingosgit.josecr.torneoya.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuario_local")
data class UsuarioLocalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val fotoPerfilPath: String? = null    // NUEVO CAMPO
)
