package mingosgit.josecr.torneoya.data.dao

import androidx.room.*
import mingosgit.josecr.torneoya.data.entities.EquipoEntity

@Dao
interface EquipoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(equipo: EquipoEntity): Long

    @Update
    suspend fun update(equipo: EquipoEntity)

    @Delete
    suspend fun delete(equipo: EquipoEntity)

    @Query("SELECT * FROM equipo WHERE id = :id")
    suspend fun getById(id: Long): EquipoEntity?

    @Query("SELECT * FROM equipo ORDER BY id ASC")
    suspend fun getAll(): List<EquipoEntity>

    @Query("SELECT nombre FROM equipo WHERE id = :id LIMIT 1")
    suspend fun getNombreById(id: Long): String?

    @Query("""
        SELECT j.nombre 
        FROM partido_equipo_jugador AS pej
        INNER JOIN jugador AS j ON pej.jugadorId = j.id
        WHERE pej.partidoId = :partidoId AND pej.equipoId = :equipoId
        ORDER BY j.nombre ASC
    """)
    suspend fun getNombresJugadoresEquipoEnPartido(partidoId: Long, equipoId: Long): List<String>
}
