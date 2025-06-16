package mingosgit.josecr.torneoya.data.dao

import androidx.room.*
import mingosgit.josecr.torneoya.data.entities.PartidoEntity

@Dao
interface PartidoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(partido: PartidoEntity): Long // IMPORTANTE: DEVUELVE ID

    @Update
    suspend fun update(partido: PartidoEntity)

    @Delete
    suspend fun delete(partido: PartidoEntity)

    @Query("SELECT * FROM partido WHERE id = :id")
    suspend fun getPartidoById(id: Long): PartidoEntity?

    @Query("SELECT * FROM partido ORDER BY fecha DESC, horaInicio DESC")
    suspend fun getAllPartidos(): List<PartidoEntity>
}
