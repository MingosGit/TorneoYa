package mingosgit.josecr.torneoya.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notificaciones_borradas")
data class NotificacionBorradaEntity(
    @PrimaryKey(autoGenerate = false)
    val notificacionUid: String
)
