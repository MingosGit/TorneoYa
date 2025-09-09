package mingosgit.josecr.torneoya.data.dao

import androidx.room.*
import mingosgit.josecr.torneoya.data.entities.ComentarioVotoEntity

@Dao
interface ComentarioVotoDao {
    // Inserta o reemplaza un voto en un comentario y devuelve su id
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(voto: ComentarioVotoEntity): Long

    // Obtiene el voto de un usuario en un comentario
    @Query("SELECT * FROM comentario_voto WHERE comentarioId = :comentarioId AND usuarioId = :usuarioId")
    suspend fun getVotoUsuario(comentarioId: Long, usuarioId: Long): ComentarioVotoEntity?

    // Obtiene la cantidad de likes de un comentario
    @Query("SELECT COUNT(*) FROM comentario_voto WHERE comentarioId = :comentarioId AND tipo = 1")
    suspend fun getLikes(comentarioId: Long): Int

    // Obtiene la cantidad de dislikes de un comentario
    @Query("SELECT COUNT(*) FROM comentario_voto WHERE comentarioId = :comentarioId AND tipo = -1")
    suspend fun getDislikes(comentarioId: Long): Int

    // Elimina el voto de un usuario en un comentario
    @Query("DELETE FROM comentario_voto WHERE comentarioId = :comentarioId AND usuarioId = :usuarioId")
    suspend fun eliminarVoto(comentarioId: Long, usuarioId: Long)
}
