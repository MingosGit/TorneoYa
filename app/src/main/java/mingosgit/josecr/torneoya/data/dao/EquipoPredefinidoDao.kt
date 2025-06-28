package mingosgit.josecr.torneoya.data.dao

import androidx.room.*
import mingosgit.josecr.torneoya.data.entities.EquipoPredefinidoEntity
import mingosgit.josecr.torneoya.data.entities.EquipoPredefinidoJugadorCrossRef
import mingosgit.josecr.torneoya.data.entities.JugadorEntity

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
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipo(equipo: EquipoPredefinidoEntity): Long

    @Update
    suspend fun updateEquipo(equipo: EquipoPredefinidoEntity)

    @Delete
    suspend fun deleteEquipo(equipo: EquipoPredefinidoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: EquipoPredefinidoJugadorCrossRef)

    @Query("DELETE FROM equipo_predefinido_jugadorcrossref WHERE equipoPredefinidoId = :equipoId AND jugadorId = :jugadorId")
    suspend fun removeJugadorDeEquipo(equipoId: Long, jugadorId: Long)

    @Transaction
    @Query("SELECT * FROM equipo_predefinido ORDER BY id ASC")
    suspend fun getAllConJugadores(): List<EquipoPredefinidoConJugadores>

    @Transaction
    @Query("SELECT * FROM equipo_predefinido WHERE id = :id")
    suspend fun getEquipoConJugadores(id: Long): EquipoPredefinidoConJugadores?
}
