// mingosgit/josecr/torneoya/data/dao/IntegrantesDao.kt
package mingosgit.josecr.torneoya.data.dao

import androidx.room.*

import mingosgit.josecr.torneoya.data.entities.IntegranteEntity

@Dao
interface IntegrantesDao {
    @Query("SELECT * FROM integrantes WHERE equipoId = :equipoId")
    suspend fun getIntegrantesByEquipoId(equipoId: Long): List<IntegranteEntity>

    @Insert
    suspend fun insertIntegrantes(integrantes: List<IntegranteEntity>)
}
