import android.content.Context
import androidx.room.Room
import mingosgit.josecr.torneoya.data.database.TorneosDatabase

object DatabaseProvider {
    @Volatile
    private var INSTANCE: TorneosDatabase? = null

    fun getDatabase(context: Context): TorneosDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                TorneosDatabase::class.java,
                "torneos_database"
            )
                .fallbackToDestructiveMigration() // <--- ESTA LÍNEA
                .build()
            INSTANCE = instance
            instance
        }
    }
}
