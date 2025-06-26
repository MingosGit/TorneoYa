package mingosgit.josecr.torneoya.repository

import mingosgit.josecr.torneoya.data.dao.EventoDao
import mingosgit.josecr.torneoya.data.entities.EventoEntity

class EventoRepository(private val eventoDao: EventoDao) {
    suspend fun agregarEvento(evento: EventoEntity) = eventoDao.insert(evento)
    suspend fun getEventosPorPartido(partidoId: Long) = eventoDao.getEventosPorPartido(partidoId)
}
