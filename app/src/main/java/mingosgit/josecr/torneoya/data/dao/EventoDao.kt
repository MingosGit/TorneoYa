package mingosgit.josecr.torneoya.data.dao

import androidx.room.*
import mingosgit.josecr.torneoya.data.entities.EventoEntity

@Dao
interface EventoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(evento: EventoEntity): Long

    @Query("SELECT * FROM evento WHERE partidoId = :partidoId ORDER BY minuto ASC, id ASC")
    suspend fun getEventosPorPartido(partidoId: Long): List<EventoEntity>
}
