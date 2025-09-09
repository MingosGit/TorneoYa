package mingosgit.josecr.torneoya.data.dao

import androidx.room.*
import mingosgit.josecr.torneoya.data.entities.EncuestaVotoEntity

@Dao
interface EncuestaVotoDao {
    // Inserta o reemplaza un voto y devuelve su id
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(voto: EncuestaVotoEntity): Long

    // Obtiene la cantidad de votos por cada opción de una encuesta
    @Query("SELECT opcionIndex, COUNT(*) as votos FROM encuesta_voto WHERE encuestaId = :encuestaId GROUP BY opcionIndex")
    suspend fun getVotosPorOpcion(encuestaId: Long): List<VotoOpcionCount>

    // Obtiene el voto (opción elegida) de un usuario en una encuesta
    @Query("SELECT opcionIndex FROM encuesta_voto WHERE encuestaId = :encuestaId AND usuarioId = :usuarioId LIMIT 1")
    suspend fun getVotoUsuario(encuestaId: Long, usuarioId: Long): Int?

    // Elimina el voto de un usuario en una encuesta
    @Query("DELETE FROM encuesta_voto WHERE encuestaId = :encuestaId AND usuarioId = :usuarioId")
    suspend fun eliminarVotoUsuario(encuestaId: Long, usuarioId: Long)
}

// Representa el número de votos agrupados por opción
data class VotoOpcionCount(
    val opcionIndex: Int,
    val votos: Int
)
