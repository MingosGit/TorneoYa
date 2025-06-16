package mingosgit.josecr.torneoya.repository

import mingosgit.josecr.torneoya.data.dao.EquipoDao
import mingosgit.josecr.torneoya.data.entities.EquipoEntity

class EquipoRepository(private val equipoDao: EquipoDao) {
    suspend fun insertEquipo(equipo: EquipoEntity) = equipoDao.insert(equipo)
    suspend fun updateEquipo(equipo: EquipoEntity) = equipoDao.update(equipo)
    suspend fun deleteEquipo(equipo: EquipoEntity) = equipoDao.delete(equipo)
    suspend fun getById(id: Long) = equipoDao.getById(id)
    suspend fun getAll() = equipoDao.getAll()
}
