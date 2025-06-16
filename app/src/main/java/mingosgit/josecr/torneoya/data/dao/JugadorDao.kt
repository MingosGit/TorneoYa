package mingosgit.josecr.torneoya.data.dao

import androidx.room.*
import mingosgit.josecr.torneoya.data.entities.JugadorEntity

@Dao
interface JugadorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(jugador: JugadorEntity): Long

    @Update
    suspend fun update(jugador: JugadorEntity)

    @Delete
    suspend fun delete(jugador: JugadorEntity)

    @Query("SELECT * FROM jugador WHERE id = :id")
    suspend fun getById(id: Long): JugadorEntity?

    @Query("SELECT * FROM jugador ORDER BY nombre ASC")
    suspend fun getAll(): List<JugadorEntity>
}
