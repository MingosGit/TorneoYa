package mingosgit.josecr.torneoya.data.dao

import androidx.room.*
import mingosgit.josecr.torneoya.data.entities.EncuestaEntity

@Dao
interface EncuestaDao {
    @Insert
    suspend fun insert(encuesta: EncuestaEntity): Long

    @Query("SELECT * FROM encuesta WHERE partidoId = :partidoId ORDER BY id DESC")
    suspend fun getEncuestasDePartido(partidoId: Long): List<EncuestaEntity>
}
