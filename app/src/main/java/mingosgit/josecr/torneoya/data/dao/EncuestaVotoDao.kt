package mingosgit.josecr.torneoya.data.dao

import androidx.room.*
import mingosgit.josecr.torneoya.data.entities.EncuestaVotoEntity

@Dao
interface EncuestaVotoDao {
    @Insert
    suspend fun insert(voto: EncuestaVotoEntity): Long

    @Query("SELECT opcionIndex, COUNT(*) as votos FROM encuesta_voto WHERE encuestaId = :encuestaId GROUP BY opcionIndex")
    suspend fun getVotosPorOpcion(encuestaId: Long): List<VotoOpcionCount>
}

data class VotoOpcionCount(
    val opcionIndex: Int,
    val votos: Int
)
