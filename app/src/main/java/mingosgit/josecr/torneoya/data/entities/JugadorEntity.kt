package mingosgit.josecr.torneoya.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jugador")
data class JugadorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String
)

