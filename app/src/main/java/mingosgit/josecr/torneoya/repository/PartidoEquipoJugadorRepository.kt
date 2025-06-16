package mingosgit.josecr.torneoya.repository

import mingosgit.josecr.torneoya.data.dao.PartidoEquipoJugadorDao
import mingosgit.josecr.torneoya.data.entities.PartidoEquipoJugadorEntity

class PartidoEquipoJugadorRepository(private val dao: PartidoEquipoJugadorDao) {
    suspend fun insert(rel: PartidoEquipoJugadorEntity) = dao.insert(rel)
    suspend fun eliminarJugadoresDeEquipo(partidoId: Long, equipoId: Long) {
        // Borra todas las relaciones de ese equipo en el partido
        val jugadores = dao.getJugadoresDeEquipoEnPartido(partidoId, equipoId)
        for (jugador in jugadores) {
            dao.delete(
                PartidoEquipoJugadorEntity(
                    partidoId = partidoId,
                    equipoId = equipoId,
                    jugadorId = jugador.id
                )
            )
        }
    }
}
