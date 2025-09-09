package mingosgit.josecr.torneoya.repository

import mingosgit.josecr.torneoya.data.dao.EstadisticasDao
import mingosgit.josecr.torneoya.data.entities.EstadisticasJugador

class EstadisticasRepository(private val estadisticasDao: EstadisticasDao) {

    suspend fun getEstadisticasJugador(jugadorId: Long): EstadisticasJugador? {
        return estadisticasDao.getEstadisticasJugador(jugadorId)
    }
}
