package mingosgit.josecr.torneoya.data.dao

import androidx.room.*
import mingosgit.josecr.torneoya.data.entities.EquipoEntity

@Dao
interface EquipoDao {
    // Inserta o reemplaza un equipo en la BD y devuelve su id
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(equipo: EquipoEntity): Long

    // Actualiza los datos de un equipo existente
    @Update
    suspend fun update(equipo: EquipoEntity)

    // Elimina un equipo de la BD
    @Delete
    suspend fun delete(equipo: EquipoEntity)

    // Obtiene un equipo por su id
    @Query("SELECT * FROM equipo WHERE id = :id")
    suspend fun getById(id: Long): EquipoEntity?

    // Obtiene todos los equipos ordenados por id ascendente
    @Query("SELECT * FROM equipo ORDER BY id ASC")
    suspend fun getAll(): List<EquipoEntity>

    // Obtiene solo el nombre de un equipo por id
    @Query("SELECT nombre FROM equipo WHERE id = :id LIMIT 1")
    suspend fun getNombreById(id: Long): String?

    // Obtiene los nombres de los jugadores de un equipo en un partido dado
    @Query("""
        SELECT j.nombre 
        FROM partido_equipo_jugador AS pej
        INNER JOIN jugador AS j ON pej.jugadorId = j.id
        WHERE pej.partidoId = :partidoId AND pej.equipoId = :equipoId
        ORDER BY j.nombre ASC
    """)
    suspend fun getNombresJugadoresEquipoEnPartido(partidoId: Long, equipoId: Long): List<String>
}
