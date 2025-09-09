package mingosgit.josecr.torneoya.data.dao

import androidx.room.*
import mingosgit.josecr.torneoya.data.entities.PartidoEquipoJugadorEntity

@Dao
interface PartidoEquipoJugadorDao {
    // Inserta o reemplaza la relación partido-equipo-jugador
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rel: PartidoEquipoJugadorEntity)

    // Elimina una relación partido-equipo-jugador
    @Delete
    suspend fun delete(rel: PartidoEquipoJugadorEntity)

    // Obtiene los jugadores de un equipo en un partido específico
    @Query("SELECT * FROM partido_equipo_jugador WHERE partidoId = :partidoId AND equipoId = :equipoId")
    suspend fun getJugadoresDeEquipoEnPartido(partidoId: Long, equipoId: Long): List<PartidoEquipoJugadorEntity>

    // Obtiene todos los jugadores de un partido
    @Query("SELECT * FROM partido_equipo_jugador WHERE partidoId = :partidoId")
    suspend fun getJugadoresDePartido(partidoId: Long): List<PartidoEquipoJugadorEntity>
}
