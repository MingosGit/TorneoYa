package mingosgit.josecr.torneoya.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import mingosgit.josecr.torneoya.data.dao.EquiposDao
import mingosgit.josecr.torneoya.data.dao.IntegrantesDao
import mingosgit.josecr.torneoya.data.dao.PartidosDao
import mingosgit.josecr.torneoya.data.dao.TorneosDao
import mingosgit.josecr.torneoya.data.entities.EquipoEntity
import mingosgit.josecr.torneoya.data.entities.PartidoEntity
import mingosgit.josecr.torneoya.data.entities.TorneoEntity
import mingosgit.josecr.torneoya.data.entities.IntegranteEntity // <-- FALTA ESTA IMPORTACIÓN

@Database(
    entities = [
        EquipoEntity::class,
        PartidoEntity::class,
        TorneoEntity::class,
        IntegranteEntity::class
    ],
    version = 2,   // <-- SUBE EL NÚMERO DE VERSIÓN
    exportSchema = false
)

abstract class TorneosDatabase : RoomDatabase() {
    abstract fun equiposDao(): EquiposDao
    abstract fun partidosDao(): PartidosDao
    abstract fun torneosDao(): TorneosDao
    abstract fun integrantesDao(): IntegrantesDao
}
