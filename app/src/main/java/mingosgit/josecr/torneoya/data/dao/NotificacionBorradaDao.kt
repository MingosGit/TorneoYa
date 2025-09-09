package mingosgit.josecr.torneoya.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import mingosgit.josecr.torneoya.data.entities.NotificacionBorradaEntity

@Dao
interface NotificacionBorradaDao {
    // Inserta o reemplaza una notificación borrada
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun borrar(notificacion: NotificacionBorradaEntity)

    // Obtiene los UID de todas las notificaciones borradas
    @Query("SELECT notificacionUid FROM notificaciones_borradas")
    suspend fun getBorradasUids(): List<String>
}
