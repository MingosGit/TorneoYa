package mingosgit.josecr.torneoya.data.dao

import androidx.room.*
import androidx.room.Dao
import mingosgit.josecr.torneoya.data.entities.EquipoEntity

@Dao
interface EquiposDao {
    @Query("SELECT * FROM equipos")
    suspend fun getAllEquipos(): List<EquipoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipo(equipo: EquipoEntity): Long

    @Update
    suspend fun updateEquipo(equipo: EquipoEntity)

    @Delete
    suspend fun deleteEquipo(equipo: EquipoEntity)

    @Query("SELECT * FROM equipos WHERE id = :id")
    suspend fun getEquipoById(id: Long): EquipoEntity?
}
