package mingosgit.josecr.torneoya.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import mingosgit.josecr.torneoya.data.dao.EquipoDao // <--- Agrega esto si no está
import mingosgit.josecr.torneoya.data.dao.JugadorDao
import mingosgit.josecr.torneoya.data.dao.PartidoDao
import mingosgit.josecr.torneoya.data.dao.PartidoEquipoJugadorDao
import mingosgit.josecr.torneoya.data.dao.UsuarioLocalDao
import mingosgit.josecr.torneoya.data.entities.EquipoEntity // <--- Agrega esto si no está
import mingosgit.josecr.torneoya.data.entities.JugadorEntity
import mingosgit.josecr.torneoya.data.entities.PartidoEntity
import mingosgit.josecr.torneoya.data.entities.PartidoEquipoJugadorEntity
import mingosgit.josecr.torneoya.data.entities.UsuarioLocalEntity

@Database(
    entities = [
        EquipoEntity::class, // <--- Agrega esto
        JugadorEntity::class,
        PartidoEntity::class,
        PartidoEquipoJugadorEntity::class,
        UsuarioLocalEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun equipoDao(): EquipoDao // <--- Agrega esto
    abstract fun jugadorDao(): JugadorDao
    abstract fun partidoDao(): PartidoDao
    abstract fun partidoEquipoJugadorDao(): PartidoEquipoJugadorDao
    abstract fun usuarioLocalDao(): UsuarioLocalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "torneoya_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
