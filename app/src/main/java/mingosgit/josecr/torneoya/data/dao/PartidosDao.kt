package mingosgit.josecr.torneoya.data.dao

import androidx.room.*
import mingosgit.josecr.torneoya.data.entities.PartidoEntity

@Dao
interface PartidosDao {
    @Query("SELECT * FROM partidos")
    suspend fun getAllPartidos(): List<PartidoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartido(partido: PartidoEntity): Long

    @Update
    suspend fun updatePartido(partido: PartidoEntity)

    @Delete
    suspend fun deletePartido(partido: PartidoEntity)

    @Query("SELECT * FROM partidos WHERE id = :id")
    suspend fun getPartidoById(id: Long): PartidoEntity?

    @Query("SELECT * FROM partidos WHERE torneoId = :torneoId")
    suspend fun getPartidosByTorneoId(torneoId: Long): List<PartidoEntity>

    @Query("SELECT * FROM partidos WHERE torneoId IS NULL")
    suspend fun getPartidosSueltos(): List<PartidoEntity>
}
