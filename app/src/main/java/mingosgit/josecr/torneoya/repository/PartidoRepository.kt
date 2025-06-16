package mingosgit.josecr.torneoya.repository

import mingosgit.josecr.torneoya.data.dao.PartidoDao
import mingosgit.josecr.torneoya.data.dao.PartidoEquipoJugadorDao
import mingosgit.josecr.torneoya.data.entities.PartidoEntity
import mingosgit.josecr.torneoya.data.entities.PartidoEquipoJugadorEntity
import mingosgit.josecr.torneoya.data.entities.JugadorEntity

class PartidoRepository(
    private val partidoDao: PartidoDao,
    private val partidoEquipoJugadorDao: PartidoEquipoJugadorDao
) {
    // PARTIDOS
    suspend fun insertPartido(partido: PartidoEntity) = partidoDao.insert(partido)
    suspend fun updatePartido(partido: PartidoEntity) = partidoDao.update(partido)
    suspend fun deletePartido(partido: PartidoEntity) = partidoDao.delete(partido)
    suspend fun getPartidoById(id: Long) = partidoDao.getPartidoById(id)
    suspend fun getAllPartidos() = partidoDao.getAllPartidos()

    // JUGADORES EN PARTIDO
    suspend fun asignarJugadorAPartido(rel: PartidoEquipoJugadorEntity) = partidoEquipoJugadorDao.insert(rel)
    suspend fun eliminarJugadorDePartido(rel: PartidoEquipoJugadorEntity) = partidoEquipoJugadorDao.delete(rel)
    suspend fun getJugadoresDeEquipoEnPartido(partidoId: Long, equipoId: Long): List<JugadorEntity> =
        partidoEquipoJugadorDao.getJugadoresDeEquipoEnPartido(partidoId, equipoId)

    suspend fun eliminarJugadoresDeEquipo(partidoId: Long, equipoId: Long) {
        val relaciones = partidoEquipoJugadorDao.getJugadoresDeEquipoEnPartido(partidoId, equipoId)
        for (rel in relaciones) {
            partidoEquipoJugadorDao.delete(PartidoEquipoJugadorEntity(partidoId, equipoId, rel.id))
        }
    }
}
