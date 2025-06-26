package mingosgit.josecr.torneoya.data.dao

import androidx.room.*
import mingosgit.josecr.torneoya.data.entities.GoleadorEntity

@Dao
interface GoleadorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goleador: GoleadorEntity): Long

    @Delete
    suspend fun delete(goleador: GoleadorEntity)

    @Query("SELECT * FROM goleador WHERE partidoId = :partidoId")
    suspend fun getGolesPorPartido(partidoId: Long): List<GoleadorEntity>

    @Query("SELECT * FROM goleador WHERE partidoId = :partidoId AND equipoId = :equipoId")
    suspend fun getGolesPorEquipoEnPartido(partidoId: Long, equipoId: Long): List<GoleadorEntity>
}
