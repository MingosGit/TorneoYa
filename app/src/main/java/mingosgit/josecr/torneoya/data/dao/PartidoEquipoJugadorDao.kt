package mingosgit.josecr.torneoya.data.dao

import androidx.room.*
import mingosgit.josecr.torneoya.data.entities.PartidoEquipoJugadorEntity

@Dao
interface PartidoEquipoJugadorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rel: PartidoEquipoJugadorEntity)

    @Delete
    suspend fun delete(rel: PartidoEquipoJugadorEntity)

    @Query("SELECT * FROM partido_equipo_jugador WHERE partidoId = :partidoId AND equipoId = :equipoId")
    suspend fun getJugadoresDeEquipoEnPartido(partidoId: Long, equipoId: Long): List<PartidoEquipoJugadorEntity>

    @Query("SELECT * FROM partido_equipo_jugador WHERE partidoId = :partidoId")
    suspend fun getJugadoresDePartido(partidoId: Long): List<PartidoEquipoJugadorEntity>
}
