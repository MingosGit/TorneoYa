package mingosgit.josecr.torneoya.repository

import mingosgit.josecr.torneoya.data.dao.EquipoPredefinidoDao
import mingosgit.josecr.torneoya.data.entities.EquipoPredefinidoEntity
import mingosgit.josecr.torneoya.data.entities.EquipoPredefinidoJugadorCrossRef

class EquipoPredefinidoRepository(
    private val dao: EquipoPredefinidoDao
) {
    suspend fun getAllConJugadores() = dao.getAllConJugadores()
    suspend fun getEquipoConJugadores(id: Long) = dao.getEquipoConJugadores(id)
    suspend fun insertEquipo(equipo: EquipoPredefinidoEntity) = dao.insertEquipo(equipo)
    suspend fun updateEquipo(equipo: EquipoPredefinidoEntity) = dao.updateEquipo(equipo)
    suspend fun deleteEquipo(equipo: EquipoPredefinidoEntity) = dao.deleteEquipo(equipo)
    suspend fun agregarJugador(equipoId: Long, jugadorId: Long) = dao.insertCrossRef(
        EquipoPredefinidoJugadorCrossRef(equipoId, jugadorId)
    )
    suspend fun quitarJugador(equipoId: Long, jugadorId: Long) = dao.removeJugadorDeEquipo(equipoId, jugadorId)
}
