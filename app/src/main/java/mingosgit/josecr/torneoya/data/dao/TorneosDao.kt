package mingosgit.josecr.torneoya.data.dao

import androidx.room.*
import mingosgit.josecr.torneoya.data.entities.TorneoEntity

@Dao
interface TorneosDao {
    @Query("SELECT * FROM torneos")
    suspend fun getAllTorneos(): List<TorneoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTorneo(torneo: TorneoEntity): Long

    @Update
    suspend fun updateTorneo(torneo: TorneoEntity)

    @Delete
    suspend fun deleteTorneo(torneo: TorneoEntity)

    @Query("SELECT * FROM torneos WHERE id = :id")
    suspend fun getTorneoById(id: Long): TorneoEntity?
}
