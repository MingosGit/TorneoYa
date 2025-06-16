package mingosgit.josecr.torneoya.repository

import mingosgit.josecr.torneoya.data.dao.JugadorDao
import mingosgit.josecr.torneoya.data.entities.JugadorEntity

class JugadorRepository(private val jugadorDao: JugadorDao) {
    suspend fun insertJugador(jugador: JugadorEntity) = jugadorDao.insert(jugador)
    suspend fun updateJugador(jugador: JugadorEntity) = jugadorDao.update(jugador)
    suspend fun deleteJugador(jugador: JugadorEntity) = jugadorDao.delete(jugador)
    suspend fun getById(id: Long) = jugadorDao.getById(id)
    suspend fun getAll() = jugadorDao.getAll()
}
