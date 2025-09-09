package mingosgit.josecr.torneoya.ui.screens.home

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val HOME_DS = "home_cache_store"

private val Context.homeDataStore: DataStore<Preferences> by preferencesDataStore(name = HOME_DS)

data class HomeCachedStats(
    val partidosTotales: Int = 0,
    val equiposTotales: Int = 0,
    val jugadoresTotales: Int = 0,
    val amigosTotales: Int = 0
)

class HomeCacheStore(private val appContext: Context) {
    private object Keys {
        val PARTIDOS = intPreferencesKey("partidos_totales")
        val EQUIPOS = intPreferencesKey("equipos_totales")
        val JUGADORES = intPreferencesKey("jugadores_totales")
        val AMIGOS = intPreferencesKey("amigos_totales")
    }

    val stats: Flow<HomeCachedStats> = appContext.homeDataStore.data.map { p ->
        HomeCachedStats(
            partidosTotales = p[Keys.PARTIDOS] ?: 0,
            equiposTotales = p[Keys.EQUIPOS] ?: 0,
            jugadoresTotales = p[Keys.JUGADORES] ?: 0,
            amigosTotales = p[Keys.AMIGOS] ?: 0
        )
    }

    suspend fun save(stats: HomeCachedStats) {
        appContext.homeDataStore.edit { p ->
            p[Keys.PARTIDOS] = stats.partidosTotales
            p[Keys.EQUIPOS] = stats.equiposTotales
            p[Keys.JUGADORES] = stats.jugadoresTotales
            p[Keys.AMIGOS] = stats.amigosTotales
        }
    }
}
