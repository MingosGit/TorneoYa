package mingosgit.josecr.torneoya.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import mingosgit.josecr.torneoya.data.entities.NotificacionArchivadaEntity

@Dao
interface NotificacionArchivadaDao {
    // Archiva una notificación (insertar o reemplazar)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun archivar(notificacion: NotificacionArchivadaEntity)

    // Obtiene los UID de todas las notificaciones archivadas
    @Query("SELECT notificacionUid FROM notificaciones_archivadas")
    suspend fun getArchivadasUids(): List<String>

    // Elimina todas las notificaciones archivadas
    @Query("DELETE FROM notificaciones_archivadas")
    suspend fun limpiarTodas()

    // Elimina una notificación archivada por su UID
    @Query("DELETE FROM notificaciones_archivadas WHERE notificacionUid = :uid")
    suspend fun limpiarNotificacion(uid: String)
}
