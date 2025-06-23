package mingosgit.josecr.torneoya.repository

import mingosgit.josecr.torneoya.data.dao.EncuestaDao
import mingosgit.josecr.torneoya.data.dao.EncuestaVotoDao
import mingosgit.josecr.torneoya.data.entities.EncuestaEntity
import mingosgit.josecr.torneoya.data.entities.EncuestaVotoEntity
import mingosgit.josecr.torneoya.data.dao.VotoOpcionCount

class EncuestaRepository(
    private val encuestaDao: EncuestaDao,
    private val encuestaVotoDao: EncuestaVotoDao
) {
    suspend fun agregarEncuesta(encuesta: EncuestaEntity) = encuestaDao.insert(encuesta)
    suspend fun obtenerEncuestas(partidoId: Long) = encuestaDao.getEncuestasDePartido(partidoId)
    suspend fun votosPorOpcion(encuestaId: Long): List<VotoOpcionCount> = encuestaVotoDao.getVotosPorOpcion(encuestaId)

    suspend fun votarUnico(encuestaId: Long, opcionIndex: Int, usuarioId: Long) {
        encuestaVotoDao.eliminarVotoUsuario(encuestaId, usuarioId)
        encuestaVotoDao.insert(
            EncuestaVotoEntity(
                encuestaId = encuestaId,
                opcionIndex = opcionIndex,
                usuarioId = usuarioId
            )
        )
    }

    suspend fun getVotoUsuario(encuestaId: Long, usuarioId: Long): Int? =
        encuestaVotoDao.getVotoUsuario(encuestaId, usuarioId)
}
