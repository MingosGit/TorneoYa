package mingosgit.josecr.torneoya.ui.screens.home

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val HOME_DS = "home_cache_store" // Nombre del DataStore para la pantalla Home

// Extensión de Context para acceder al DataStore de preferencias de Home
private val Context.homeDataStore: DataStore<Preferences> by preferencesDataStore(name = HOME_DS)

// DTO con las métricas cacheadas que se muestran en Home
data class HomeCachedStats(
    val partidosTotales: Int = 0,
    val equiposTotales: Int = 0,
    val jugadoresTotales: Int = 0,
    val amigosTotales: Int = 0
)

// Capa de acceso a DataStore para leer/guardar las métricas de Home
class HomeCacheStore(private val appContext: Context) {
    // Claves de preferencias para cada contador
    private object Keys {
        val PARTIDOS = intPreferencesKey("partidos_totales")
        val EQUIPOS = intPreferencesKey("equipos_totales")
        val JUGADORES = intPreferencesKey("jugadores_totales")
        val AMIGOS = intPreferencesKey("amigos_totales")
    }

    // Flow que expone las stats leídas desde DataStore mapeadas al modelo
    val stats: Flow<HomeCachedStats> = appContext.homeDataStore.data.map { p ->
        HomeCachedStats(
            partidosTotales = p[Keys.PARTIDOS] ?: 0,
            equiposTotales = p[Keys.EQUIPOS] ?: 0,
            jugadoresTotales = p[Keys.JUGADORES] ?: 0,
            amigosTotales = p[Keys.AMIGOS] ?: 0
        )
    }

    // Guarda las stats en DataStore sobrescribiendo los valores actuales
    suspend fun save(stats: HomeCachedStats) {
        appContext.homeDataStore.edit { p ->
            p[Keys.PARTIDOS] = stats.partidosTotales
            p[Keys.EQUIPOS] = stats.equiposTotales
            p[Keys.JUGADORES] = stats.jugadoresTotales
            p[Keys.AMIGOS] = stats.amigosTotales
        }
    }
}
