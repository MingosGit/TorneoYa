package mingosgit.josecr.torneoya.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notificaciones_archivadas")
data class NotificacionArchivadaEntity(
    @PrimaryKey(autoGenerate = false)
    val notificacionUid: String // UID de la notificación archivada (único por usuario)
)
