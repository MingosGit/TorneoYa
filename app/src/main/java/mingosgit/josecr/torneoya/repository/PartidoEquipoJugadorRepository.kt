package mingosgit.josecr.torneoya.repository

import mingosgit.josecr.torneoya.data.dao.PartidoEquipoJugadorDao
import mingosgit.josecr.torneoya.data.entities.PartidoEquipoJugadorEntity

class PartidoEquipoJugadorRepository(private val partidoEquipoJugadorDao: PartidoEquipoJugadorDao) {

    suspend fun insert(partidoEquipoJugador: PartidoEquipoJugadorEntity) = partidoEquipoJugadorDao.insert(partidoEquipoJugador)
    suspend fun delete(partidoEquipoJugador: PartidoEquipoJugadorEntity) = partidoEquipoJugadorDao.delete(partidoEquipoJugador)
    suspend fun getJugadoresDeEquipoEnPartido(partidoId: Long, equipoId: Long) = partidoEquipoJugadorDao.getJugadoresDeEquipoEnPartido(partidoId, equipoId)

    suspend fun eliminarJugadoresDeEquipo(partidoId: Long, equipoId: Long) {
        val relaciones = partidoEquipoJugadorDao.getJugadoresDeEquipoEnPartido(partidoId, equipoId)
        relaciones.forEach { partidoEquipoJugadorDao.delete(it) }
    }
}
