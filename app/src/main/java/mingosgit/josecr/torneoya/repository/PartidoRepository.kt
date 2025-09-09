package mingosgit.josecr.torneoya.repository

import mingosgit.josecr.torneoya.data.dao.PartidoDao
import mingosgit.josecr.torneoya.data.dao.PartidoEquipoJugadorDao
import mingosgit.josecr.torneoya.data.dao.EquipoDao
import mingosgit.josecr.torneoya.data.dao.JugadorDao
import mingosgit.josecr.torneoya.data.entities.PartidoEntity
import mingosgit.josecr.torneoya.data.entities.PartidoEquipoJugadorEntity
import mingosgit.josecr.torneoya.data.entities.JugadorEntity

class PartidoRepository(
    private val partidoDao: PartidoDao,
    private val partidoEquipoJugadorDao: PartidoEquipoJugadorDao,
    private val equipoDao: EquipoDao,
    private val jugadorDao: JugadorDao
) {
    suspend fun insertPartido(partido: PartidoEntity) = partidoDao.insert(partido)
    suspend fun updatePartido(partido: PartidoEntity) = partidoDao.update(partido)
    suspend fun deletePartido(partido: PartidoEntity) = partidoDao.delete(partido)
    suspend fun getPartidoById(id: Long) = partidoDao.getPartidoById(id)
    suspend fun getAllPartidos() = partidoDao.getAllPartidos()

    suspend fun getJugadoresDeEquipoEnPartido(partidoId: Long, equipoId: Long): List<JugadorEntity> {
        val relaciones = partidoEquipoJugadorDao.getJugadoresDeEquipoEnPartido(partidoId, equipoId)
        return relaciones.mapNotNull { jugadorDao.getById(it.jugadorId) }
    }

    suspend fun asignarJugadorAPartido(rel: PartidoEquipoJugadorEntity) {
        partidoEquipoJugadorDao.insert(rel)
    }

    suspend fun eliminarJugadorDePartido(rel: PartidoEquipoJugadorEntity) {
        partidoEquipoJugadorDao.delete(rel)
    }


    suspend fun actualizarGoles(partidoId: Long, golesA: Int, golesB: Int) {
        partidoDao.actualizarGoles(partidoId, golesA, golesB)
    }

    // === NUEVO: Eliminar partido por id (Room) ===
    suspend fun deletePartidoById(id: Long) {
        val partido = partidoDao.getPartidoById(id)
        if (partido != null) {
            partidoDao.delete(partido)
        }
    }
}
