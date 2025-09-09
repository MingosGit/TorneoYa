package mingosgit.josecr.torneoya.data.dao

import androidx.room.*
import mingosgit.josecr.torneoya.data.entities.EquipoPredefinidoEntity
import mingosgit.josecr.torneoya.data.entities.EquipoPredefinidoJugadorCrossRef
import mingosgit.josecr.torneoya.data.entities.JugadorEntity

// Relación de un equipo predefinido con sus jugadores
data class EquipoPredefinidoConJugadores(
    @Embedded val equipo: EquipoPredefinidoEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = EquipoPredefinidoJugadorCrossRef::class,
            parentColumn = "equipoPredefinidoId",
            entityColumn = "jugadorId"
        )
    )
    val jugadores: List<JugadorEntity>
)

@Dao
interface EquipoPredefinidoDao {
    // Inserta o reemplaza un equipo predefinido y devuelve su id
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipo(equipo: EquipoPredefinidoEntity): Long

    // Actualiza los datos de un equipo predefinido
    @Update
    suspend fun updateEquipo(equipo: EquipoPredefinidoEntity)

    // Elimina un equipo predefinido
    @Delete
    suspend fun deleteEquipo(equipo: EquipoPredefinidoEntity)

    // Inserta o reemplaza la relación entre equipo y jugador
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: EquipoPredefinidoJugadorCrossRef)

    // Elimina la relación de un jugador en un equipo predefinido
    @Query("DELETE FROM equipo_predefinido_jugadorcrossref WHERE equipoPredefinidoId = :equipoId AND jugadorId = :jugadorId")
    suspend fun removeJugadorDeEquipo(equipoId: Long, jugadorId: Long)

    // Obtiene todos los equipos predefinidos con sus jugadores
    @Transaction
    @Query("SELECT * FROM equipo_predefinido ORDER BY id ASC")
    suspend fun getAllConJugadores(): List<EquipoPredefinidoConJugadores>

    // Obtiene un equipo predefinido con sus jugadores por id
    @Transaction
    @Query("SELECT * FROM equipo_predefinido WHERE id = :id")
    suspend fun getEquipoConJugadores(id: Long): EquipoPredefinidoConJugadores?
}
