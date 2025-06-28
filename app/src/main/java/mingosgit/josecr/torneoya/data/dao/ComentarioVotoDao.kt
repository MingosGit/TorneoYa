package mingosgit.josecr.torneoya.data.dao

import androidx.room.*
import mingosgit.josecr.torneoya.data.entities.ComentarioVotoEntity

@Dao
interface ComentarioVotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(voto: ComentarioVotoEntity): Long

    @Query("SELECT * FROM comentario_voto WHERE comentarioId = :comentarioId AND usuarioId = :usuarioId")
    suspend fun getVotoUsuario(comentarioId: Long, usuarioId: Long): ComentarioVotoEntity?

    @Query("SELECT COUNT(*) FROM comentario_voto WHERE comentarioId = :comentarioId AND tipo = 1")
    suspend fun getLikes(comentarioId: Long): Int

    @Query("SELECT COUNT(*) FROM comentario_voto WHERE comentarioId = :comentarioId AND tipo = -1")
    suspend fun getDislikes(comentarioId: Long): Int

    @Query("DELETE FROM comentario_voto WHERE comentarioId = :comentarioId AND usuarioId = :usuarioId")
    suspend fun eliminarVoto(comentarioId: Long, usuarioId: Long)
}
