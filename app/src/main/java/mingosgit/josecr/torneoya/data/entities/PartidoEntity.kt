package mingosgit.josecr.torneoya.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "partido")
data class PartidoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fecha: String,
    val horaInicio: String,
    val numeroPartes: Int,
    val tiempoPorParte: Int,
    val tiempoDescanso: Int, // Nuevo campo para el descanso entre partes
    val equipoAId: Long,
    val equipoBId: Long,
    val numeroJugadores: Int,
    val estado: PartidoEstado = PartidoEstado.PREVIA // Nuevo campo de estado
)

enum class PartidoEstado {
    PREVIA, JUGANDO, FINALIZADO
}
