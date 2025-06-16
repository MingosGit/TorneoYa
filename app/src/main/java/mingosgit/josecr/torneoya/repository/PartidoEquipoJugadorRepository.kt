// mingosgit.josecr.torneoya.repository.PartidoEquipoJugadorRepository.kt
package mingosgit.josecr.torneoya.repository

import mingosgit.josecr.torneoya.data.dao.PartidoEquipoJugadorDao
import mingosgit.josecr.torneoya.data.entities.PartidoEquipoJugadorEntity

class PartidoEquipoJugadorRepository(private val dao: PartidoEquipoJugadorDao) {
    suspend fun insert(rel: PartidoEquipoJugadorEntity) = dao.insert(rel)
}
