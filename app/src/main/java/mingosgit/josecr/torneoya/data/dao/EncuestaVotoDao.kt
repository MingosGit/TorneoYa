package mingosgit.josecr.torneoya.data.dao

import androidx.room.*
import mingosgit.josecr.torneoya.data.entities.EncuestaVotoEntity

@Dao
interface EncuestaVotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(voto: EncuestaVotoEntity): Long

    @Query("SELECT opcionIndex, COUNT(*) as votos FROM encuesta_voto WHERE encuestaId = :encuestaId GROUP BY opcionIndex")
    suspend fun getVotosPorOpcion(encuestaId: Long): List<VotoOpcionCount>

    @Query("SELECT opcionIndex FROM encuesta_voto WHERE encuestaId = :encuestaId AND usuarioId = :usuarioId LIMIT 1")
    suspend fun getVotoUsuario(encuestaId: Long, usuarioId: Long): Int?

    @Query("DELETE FROM encuesta_voto WHERE encuestaId = :encuestaId AND usuarioId = :usuarioId")
    suspend fun eliminarVotoUsuario(encuestaId: Long, usuarioId: Long)
}

data class VotoOpcionCount(
    val opcionIndex: Int,
    val votos: Int
)
