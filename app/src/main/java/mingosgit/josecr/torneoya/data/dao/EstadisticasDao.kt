package mingosgit.josecr.torneoya.data.dao

import androidx.room.Dao
import androidx.room.Query
import mingosgit.josecr.torneoya.data.entities.EstadisticasJugador

@Dao
interface EstadisticasDao {

    // Obtiene estadísticas de todos los jugadores (goles, asistencias y partidos jugados)
    @Query("""
        SELECT 
            j.id AS jugadorId,
            j.nombre AS nombre,
            COUNT(DISTINCT pej.partidoId) AS partidosJugados,
            COALESCE((
                SELECT COUNT(*) FROM goleador g WHERE g.jugadorId = j.id
            ), 0) AS goles,
            COALESCE((
                SELECT COUNT(*) FROM goleador g2 WHERE g2.asistenciaJugadorId = j.id
            ), 0) AS asistencias
        FROM jugador j
        LEFT JOIN partido_equipo_jugador pej ON pej.jugadorId = j.id
        GROUP BY j.id, j.nombre
        ORDER BY goles DESC, asistencias DESC, partidosJugados DESC
    """)
    suspend fun getEstadisticasTodos(): List<EstadisticasJugador>

    // Obtiene estadísticas de un jugador específico por su id
    @Query("""
        SELECT 
            j.id AS jugadorId,
            j.nombre AS nombre,
            COUNT(DISTINCT pej.partidoId) AS partidosJugados,
            COALESCE((
                SELECT COUNT(*) FROM goleador g WHERE g.jugadorId = j.id
            ), 0) AS goles,
            COALESCE((
                SELECT COUNT(*) FROM goleador g2 WHERE g2.asistenciaJugadorId = j.id
            ), 0) AS asistencias
        FROM jugador j
        LEFT JOIN partido_equipo_jugador pej ON pej.jugadorId = j.id
        WHERE j.id = :jugadorId
        GROUP BY j.id, j.nombre
    """)
    suspend fun getEstadisticasJugador(jugadorId: Long): EstadisticasJugador?
}
