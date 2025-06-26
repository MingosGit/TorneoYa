package mingosgit.josecr.torneoya.repository

import mingosgit.josecr.torneoya.data.dao.GoleadorDao
import mingosgit.josecr.torneoya.data.entities.GoleadorEntity

class GoleadorRepository(
    private val goleadorDao: GoleadorDao
) {
    suspend fun insertarGol(
        partidoId: Long,
        equipoId: Long,
        jugadorId: Long,
        minuto: Int?,
        asistenciaJugadorId: Long?
    ): Long {
        val gol = GoleadorEntity(
            partidoId = partidoId,
            equipoId = equipoId,
            jugadorId = jugadorId,
            minuto = minuto,
            asistenciaJugadorId = asistenciaJugadorId
        )
        return goleadorDao.insert(gol)
    }

    suspend fun borrarGol(gol: GoleadorEntity) = goleadorDao.delete(gol)
    suspend fun getGolesPorPartido(partidoId: Long) = goleadorDao.getGolesPorPartido(partidoId)
    suspend fun getGolesPorEquipoEnPartido(partidoId: Long, equipoId: Long) =
        goleadorDao.getGolesPorEquipoEnPartido(partidoId, equipoId)
}
