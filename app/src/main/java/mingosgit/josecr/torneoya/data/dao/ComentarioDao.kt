package mingosgit.josecr.torneoya.data.dao

import androidx.room.*
import mingosgit.josecr.torneoya.data.entities.ComentarioEntity

@Dao
interface ComentarioDao {
    @Insert
    suspend fun insert(comentario: ComentarioEntity): Long

    @Query("SELECT * FROM comentario WHERE partidoId = :partidoId ORDER BY fechaHora DESC")
    suspend fun getComentariosDePartido(partidoId: Long): List<ComentarioEntity>
}
