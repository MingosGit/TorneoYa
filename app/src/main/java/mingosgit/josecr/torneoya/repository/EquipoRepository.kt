package mingosgit.josecr.torneoya.repository

import mingosgit.josecr.torneoya.data.dao.EquipoDao
import mingosgit.josecr.torneoya.data.dao.PartidoEquipoJugadorDao
import mingosgit.josecr.torneoya.data.dao.JugadorDao
import mingosgit.josecr.torneoya.data.entities.EquipoEntity

class EquipoRepository(
    private val equipoDao: EquipoDao,
    private val partidoEquipoJugadorDao: PartidoEquipoJugadorDao,
    private val jugadorDao: JugadorDao
) {
    suspend fun insertEquipo(equipo: EquipoEntity) = equipoDao.insert(equipo)
    suspend fun updateEquipo(equipo: EquipoEntity) = equipoDao.update(equipo)
    suspend fun getById(id: Long) = equipoDao.getById(id)
    suspend fun getAll() = equipoDao.getAll()

    suspend fun getNombresJugadoresEquipoEnPartido(partidoId: Long, equipoId: Long): List<String> {
        val relaciones = partidoEquipoJugadorDao.getJugadoresDeEquipoEnPartido(partidoId, equipoId)
        val jugadores = relaciones.mapNotNull { rel ->
            jugadorDao.getById(rel.jugadorId)
        }
        return jugadores.map { it.nombre }
    }
}
