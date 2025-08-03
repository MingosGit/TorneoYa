package mingosgit.josecr.torneoya.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import mingosgit.josecr.torneoya.data.entities.NotificacionArchivadaEntity

@Dao
interface NotificacionArchivadaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun archivar(notificacion: NotificacionArchivadaEntity)

    @Query("SELECT notificacionUid FROM notificaciones_archivadas")
    suspend fun getArchivadasUids(): List<String>

    @Query("DELETE FROM notificaciones_archivadas")
    suspend fun limpiarTodas()

    @Query("DELETE FROM notificaciones_archivadas WHERE notificacionUid = :uid")
    suspend fun limpiarNotificacion(uid: String)

}
