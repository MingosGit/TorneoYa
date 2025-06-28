package mingosgit.josecr.torneoya.repository

import mingosgit.josecr.torneoya.data.dao.ComentarioDao
import mingosgit.josecr.torneoya.data.dao.ComentarioVotoDao
import mingosgit.josecr.torneoya.data.entities.ComentarioEntity
import mingosgit.josecr.torneoya.data.entities.ComentarioVotoEntity

class ComentarioRepository(
    private val comentarioDao: ComentarioDao,
    private val comentarioVotoDao: ComentarioVotoDao
) {
    suspend fun agregarComentario(comentario: ComentarioEntity) = comentarioDao.insert(comentario)
    suspend fun obtenerComentarios(partidoId: Long) = comentarioDao.getComentariosDePartido(partidoId)

    suspend fun votarComentario(comentarioId: Long, usuarioId: Long, tipo: Int) {
        // Solo un voto por usuario y comentario, sobrescribe o reemplaza
        comentarioVotoDao.eliminarVoto(comentarioId, usuarioId)
        val voto = ComentarioVotoEntity(
            comentarioId = comentarioId,
            usuarioId = usuarioId,
            tipo = tipo
        )
        comentarioVotoDao.insert(voto)
    }

    suspend fun getVotoUsuario(comentarioId: Long, usuarioId: Long): ComentarioVotoEntity? =
        comentarioVotoDao.getVotoUsuario(comentarioId, usuarioId)

    suspend fun getLikes(comentarioId: Long): Int = comentarioVotoDao.getLikes(comentarioId)
    suspend fun getDislikes(comentarioId: Long): Int = comentarioVotoDao.getDislikes(comentarioId)
}
