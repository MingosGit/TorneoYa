package mingosgit.josecr.torneoya.data.dao

import androidx.room.*
import mingosgit.josecr.torneoya.data.entities.PartidoEquipoJugadorEntity
import mingosgit.josecr.torneoya.data.entities.JugadorEntity

@Dao
interface PartidoEquipoJugadorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rel: PartidoEquipoJugadorEntity)

    @Delete
    suspend fun delete(rel: PartidoEquipoJugadorEntity)

    // AQUÍ EL JOIN PARA OBTENER LOS JUGADORES COMPLETOS
    @Query("""
        SELECT jugador.* FROM jugador
        INNER JOIN partido_equipo_jugador ON jugador.id = partido_equipo_jugador.jugadorId
        WHERE partido_equipo_jugador.partidoId = :partidoId AND partido_equipo_jugador.equipoId = :equipoId
    """)
    suspend fun getJugadoresDeEquipoEnPartido(partidoId: Long, equipoId: Long): List<JugadorEntity>
}