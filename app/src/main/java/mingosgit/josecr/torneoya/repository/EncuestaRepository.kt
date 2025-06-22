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
    suspend fun votar(encuestaId: Long, opcionIndex: Int) = encuestaVotoDao.insert(
        EncuestaVotoEntity(encuestaId = encuestaId, opcionIndex = opcionIndex)
    )
    suspend fun votosPorOpcion(encuestaId: Long): List<VotoOpcionCount> = encuestaVotoDao.getVotosPorOpcion(encuestaId)
}
