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
    val tiempoDescanso: Int,
    val equipoAId: Long,
    val equipoBId: Long,
    val numeroJugadores: Int,
    val estado: PartidoEstado = PartidoEstado.PREVIA,
    val golesEquipoA: Int = 0,
    val golesEquipoB: Int = 0
)

enum class PartidoEstado {
    PREVIA, JUGANDO, FINALIZADO
}
