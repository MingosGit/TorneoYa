// mingosgit/josecr/torneoya/data/entities/IntegranteEntity.kt
package mingosgit.josecr.torneoya.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "integrantes")
data class IntegranteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val equipoId: Long,
    val nombre: String
)
