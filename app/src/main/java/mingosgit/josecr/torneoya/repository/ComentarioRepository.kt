package mingosgit.josecr.torneoya.repository

import mingosgit.josecr.torneoya.data.dao.ComentarioDao
import mingosgit.josecr.torneoya.data.entities.ComentarioEntity

class ComentarioRepository(private val comentarioDao: ComentarioDao) {
    suspend fun agregarComentario(comentario: ComentarioEntity) = comentarioDao.insert(comentario)
    suspend fun obtenerComentarios(partidoId: Long) = comentarioDao.getComentariosDePartido(partidoId)
}
