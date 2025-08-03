package mingosgit.josecr.torneoya.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import mingosgit.josecr.torneoya.data.entities.NotificacionBorradaEntity

@Dao
interface NotificacionBorradaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun borrar(notificacion: NotificacionBorradaEntity)

    @Query("SELECT notificacionUid FROM notificaciones_borradas")
    suspend fun getBorradasUids(): List<String>
}
