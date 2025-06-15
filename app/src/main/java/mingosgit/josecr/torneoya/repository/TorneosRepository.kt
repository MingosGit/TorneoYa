package mingosgit.josecr.torneoya.repository

import mingosgit.josecr.torneoya.data.dao.TorneosDao
import mingosgit.josecr.torneoya.data.entities.TorneoEntity

class TorneosRepository(private val torneosDao: TorneosDao) {

    suspend fun getAllTorneos(): List<TorneoEntity> =
        torneosDao.getAllTorneos()

    suspend fun getTorneoById(id: Long): TorneoEntity? =
        torneosDao.getTorneoById(id)

    suspend fun insertTorneo(torneo: TorneoEntity): Long =
        torneosDao.insertTorneo(torneo)

    suspend fun updateTorneo(torneo: TorneoEntity) =
        torneosDao.updateTorneo(torneo)

    suspend fun deleteTorneo(torneo: TorneoEntity) =
        torneosDao.deleteTorneo(torneo)
}
