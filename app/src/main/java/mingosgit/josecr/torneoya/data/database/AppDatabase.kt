package mingosgit.josecr.torneoya.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import mingosgit.josecr.torneoya.data.dao.*
import mingosgit.josecr.torneoya.data.entities.*
import mingosgit.josecr.torneoya.data.entities.GoleadorEntity
import mingosgit.josecr.torneoya.data.dao.GoleadorDao


@Database(
    entities = [
        EquipoEntity::class,
        JugadorEntity::class,
        PartidoEntity::class,
        PartidoEquipoJugadorEntity::class,
        UsuarioLocalEntity::class,
        ComentarioEntity::class,
        EncuestaEntity::class,
        EncuestaVotoEntity::class,
        GoleadorEntity::class,
        EventoEntity::class
    ],
    version = 9 // sube versión si modificaste, ej. de 8 a 9
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
    abstract fun goleadorDao(): GoleadorDao // <-- DEBE IR AQUÍ
    abstract fun eventoDao(): EventoDao
    abstract fun estadisticasDao(): EstadisticasDao

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
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
