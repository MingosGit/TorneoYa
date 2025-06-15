package mingosgit.josecr.torneoya.repository

import mingosgit.josecr.torneoya.data.dao.PartidosDao
import mingosgit.josecr.torneoya.data.entities.PartidoEntity

class PartidosRepository(private val partidosDao: PartidosDao) {

    suspend fun getAllPartidos(): List<PartidoEntity> =
        partidosDao.getAllPartidos()

    suspend fun getPartidoById(id: Long): PartidoEntity? =
        partidosDao.getPartidoById(id)

    suspend fun getPartidosByTorneoId(torneoId: Long): List<PartidoEntity> =
        partidosDao.getPartidosByTorneoId(torneoId)

    suspend fun getPartidosSueltos(): List<PartidoEntity> =
        partidosDao.getPartidosSueltos()

    suspend fun insertPartido(partido: PartidoEntity): Long =
        partidosDao.insertPartido(partido)

    suspend fun updatePartido(partido: PartidoEntity) =
        partidosDao.updatePartido(partido)

    suspend fun deletePartido(partido: PartidoEntity) =
        partidosDao.deletePartido(partido)
}
