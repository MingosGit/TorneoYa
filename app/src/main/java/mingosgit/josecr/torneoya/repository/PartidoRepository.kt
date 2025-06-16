package mingosgit.josecr.torneoya.repository

import mingosgit.josecr.torneoya.data.dao.PartidoDao
import mingosgit.josecr.torneoya.data.dao.PartidoEquipoJugadorDao
import mingosgit.josecr.torneoya.data.entities.PartidoEntity
import mingosgit.josecr.torneoya.data.entities.PartidoEquipoJugadorEntity

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
    suspend fun getJugadoresDeEquipoEnPartido(partidoId: Long, equipo: String) =
        partidoEquipoJugadorDao.getJugadoresDeEquipoEnPartido(partidoId, equipo)
    suspend fun getJugadoresDePartido(partidoId: Long) =
        partidoEquipoJugadorDao.getJugadoresDePartido(partidoId)

    suspend fun getNombresJugadoresDeEquipoEnPartido(partidoId: Long, equipo: String, jugadorDao: mingosgit.josecr.torneoya.data.dao.JugadorDao): List<String> {
        val relaciones = getJugadoresDeEquipoEnPartido(partidoId, equipo)
        return relaciones.mapNotNull { jugadorDao.getById(it.jugadorId)?.nombre }
    }
}
