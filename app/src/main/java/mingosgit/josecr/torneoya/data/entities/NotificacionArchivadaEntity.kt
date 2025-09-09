package mingosgit.josecr.torneoya.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// Entidad que representa una notificación archivada
@Entity(tableName = "notificaciones_archivadas")
data class NotificacionArchivadaEntity(
    @PrimaryKey(autoGenerate = false)
    val notificacionUid: String // UID único de la notificación archivada
)
