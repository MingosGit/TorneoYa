// mingosgit/josecr/torneoya/repository/IntegrantesRepository.kt
package mingosgit.josecr.torneoya.repository

import mingosgit.josecr.torneoya.data.dao.IntegrantesDao
import mingosgit.josecr.torneoya.data.entities.IntegranteEntity

class IntegrantesRepository(private val dao: IntegrantesDao) {
    suspend fun getIntegrantesByEquipoId(equipoId: Long): List<IntegranteEntity> =
        dao.getIntegrantesByEquipoId(equipoId)
    suspend fun insertIntegrantes(integrantes: List<IntegranteEntity>) =
        dao.insertIntegrantes(integrantes)
    suspend fun eliminarIntegrante(integrante: IntegranteEntity) =
        dao.deleteIntegrante(integrante)

}
