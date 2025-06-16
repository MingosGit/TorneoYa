package mingosgit.josecr.torneoya.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuario_local")
data class UsuarioLocalEntity(
    @PrimaryKey val id: Int = 1, // Siempre 1
    val nombre: String
)
