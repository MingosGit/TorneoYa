package mingosgit.josecr.torneoya.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import mingosgit.josecr.torneoya.data.dao.EquipoDao
import mingosgit.josecr.torneoya.data.dao.JugadorDao
import mingosgit.josecr.torneoya.data.dao.PartidoDao
import mingosgit.josecr.torneoya.data.dao.PartidoEquipoJugadorDao
import mingosgit.josecr.torneoya.data.dao.UsuarioLocalDao
import mingosgit.josecr.torneoya.data.dao.ComentarioDao
import mingosgit.josecr.torneoya.data.dao.EncuestaDao
import mingosgit.josecr.torneoya.data.dao.EncuestaVotoDao
import mingosgit.josecr.torneoya.data.entities.EquipoEntity
import mingosgit.josecr.torneoya.data.entities.JugadorEntity
import mingosgit.josecr.torneoya.data.entities.PartidoEntity
import mingosgit.josecr.torneoya.data.entities.PartidoEquipoJugadorEntity
import mingosgit.josecr.torneoya.data.entities.UsuarioLocalEntity
import mingosgit.josecr.torneoya.data.entities.ComentarioEntity
import mingosgit.josecr.torneoya.data.entities.EncuestaEntity
import mingosgit.josecr.torneoya.data.entities.EncuestaVotoEntity

@Database(
    entities = [
        EquipoEntity::class,
        JugadorEntity::class,
        PartidoEntity::class,
        PartidoEquipoJugadorEntity::class,
        UsuarioLocalEntity::class,
        ComentarioEntity::class,
        EncuestaEntity::class,
        EncuestaVotoEntity::class
    ],
    version = 5 // SUBE LA VERSIÓN PORQUE CAMBIASTE EL ESQUEMA!!!
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun equipoDao(): EquipoDao
    abstract fun jugadorDao(): JugadorDao
    abstract fun partidoDao(): PartidoDao
    abstract fun partidoEquipoJugadorDao(): PartidoEquipoJugadorDao
    abstract fun usuarioLocalDao(): UsuarioLocalDao
    abstract fun comentarioDao(): ComentarioDao
    abstract fun encuestaDao(): EncuestaDao
    abstract fun encuestaVotoDao(): EncuestaVotoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "torneoya_db"
                )
                    .fallbackToDestructiveMigration() // Para DEV rápido, destruye y recrea todo
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
