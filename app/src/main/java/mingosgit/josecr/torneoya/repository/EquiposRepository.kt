package mingosgit.josecr.torneoya.repository

import mingosgit.josecr.torneoya.data.dao.EquiposDao
import mingosgit.josecr.torneoya.data.entities.EquipoEntity

class EquiposRepository(private val equiposDao: EquiposDao) {

    suspend fun getAllEquipos(): List<EquipoEntity> =
        equiposDao.getAllEquipos()

    suspend fun getEquipoById(id: Long): EquipoEntity? =
        equiposDao.getEquipoById(id)

    suspend fun insertEquipo(equipo: EquipoEntity): Long =
        equiposDao.insertEquipo(equipo)

    suspend fun updateEquipo(equipo: EquipoEntity) =
        equiposDao.updateEquipo(equipo)

    suspend fun deleteEquipo(equipo: EquipoEntity) =
        equiposDao.deleteEquipo(equipo)
}
