package mingosgit.josecr.torneoya.data.dao

import androidx.room.*
import mingosgit.josecr.torneoya.data.entities.EquipoEntity

@Dao
interface EquipoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(equipo: EquipoEntity): Long

    @Update
    suspend fun update(equipo: EquipoEntity)

    @Delete
    suspend fun delete(equipo: EquipoEntity)

    @Query("SELECT * FROM equipo WHERE id = :id")
    suspend fun getById(id: Long): EquipoEntity?

    @Query("SELECT * FROM equipo ORDER BY id ASC")
    suspend fun getAll(): List<EquipoEntity>
}
