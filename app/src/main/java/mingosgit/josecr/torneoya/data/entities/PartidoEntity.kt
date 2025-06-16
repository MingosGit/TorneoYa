package mingosgit.josecr.torneoya.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "partido")
data class PartidoEntity(
    @PrimaryKey val id: Long, // timestamp personalizado
    val fecha: String, // yyyy-MM-dd
    val horaInicio: String, // HH:mm
    val numeroPartes: Int,
    val tiempoPorParte: Int, // minutos
    val equipoA: String, // nombre o id equipo, puedes cambiar a Int si usas ids
    val equipoB: String,
    val numeroJugadores: Int
)
