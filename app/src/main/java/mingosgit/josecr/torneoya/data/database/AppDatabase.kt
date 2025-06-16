package mingosgit.josecr.torneoya.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import mingosgit.josecr.torneoya.data.dao.JugadorDao
import mingosgit.josecr.torneoya.data.dao.PartidoDao
import mingosgit.josecr.torneoya.data.dao.PartidoEquipoJugadorDao
import mingosgit.josecr.torneoya.data.entities.JugadorEntity
import mingosgit.josecr.torneoya.data.entities.PartidoEntity
import mingosgit.josecr.torneoya.data.entities.PartidoEquipoJugadorEntity

@Database(
    entities = [JugadorEntity::class, PartidoEntity::class, PartidoEquipoJugadorEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun jugadorDao(): JugadorDao
    abstract fun partidoDao(): PartidoDao
    abstract fun partidoEquipoJugadorDao(): PartidoEquipoJugadorDao
}
