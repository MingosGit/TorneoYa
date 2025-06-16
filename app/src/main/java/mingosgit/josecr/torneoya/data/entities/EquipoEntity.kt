package mingosgit.josecr.torneoya.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "equipo")
data class EquipoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String
)
