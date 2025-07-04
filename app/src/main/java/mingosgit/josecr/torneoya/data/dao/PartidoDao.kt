package mingosgit.josecr.torneoya.data.dao

import androidx.room.*
import mingosgit.josecr.torneoya.data.entities.PartidoEntity

@Dao
interface PartidoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(partido: PartidoEntity): Long

    @Update
    suspend fun update(partido: PartidoEntity)

    @Delete
    suspend fun delete(partido: PartidoEntity)

    @Query("SELECT * FROM partido WHERE id = :id")
    suspend fun getPartidoById(id: Long): PartidoEntity?

    @Query("SELECT * FROM partido ORDER BY fecha DESC, horaInicio DESC")
    suspend fun getAllPartidos(): List<PartidoEntity>

    @Query("UPDATE partido SET estado = :nuevoEstado WHERE id = :partidoId")
    suspend fun actualizarEstado(partidoId: Long, nuevoEstado: String)

    @Query("UPDATE partido SET golesEquipoA = :golesA, golesEquipoB = :golesB WHERE id = :partidoId")
    suspend fun actualizarGoles(partidoId: Long, golesA: Int, golesB: Int)
}
