package mingosgit.josecr.torneoya

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import mingosgit.josecr.torneoya.data.database.AppDatabase
import mingosgit.josecr.torneoya.repository.*
import mingosgit.josecr.torneoya.ui.navigation.*
import mingosgit.josecr.torneoya.ui.theme.ModernTorneoYaTheme
import mingosgit.josecr.torneoya.viewmodel.usuario.*
import mingosgit.josecr.torneoya.viewmodel.partido.*
import mingosgit.josecr.torneoya.ui.screens.home.HomeViewModel
import androidx.core.view.WindowCompat
import android.graphics.Color as AndroidColor
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var homeViewModel: HomeViewModel

    override fun attachBaseContext(newBase: Context) {
        val sharedPref = newBase.getSharedPreferences("settings", MODE_PRIVATE)
        val language = sharedPref.getString("app_language", "es") ?: "es"
        val localeUpdatedContext = updateLocale(newBase, language)
        super.attachBaseContext(localeUpdatedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_TorneoYa)
        super.onCreate(savedInstanceState)

        window.statusBarColor = AndroidColor.BLACK
        window.navigationBarColor = AndroidColor.BLACK
        WindowCompat.getInsetsController(window, window.decorView)?.isAppearanceLightStatusBars = false
        WindowCompat.setDecorFitsSystemWindows(window, false)

        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        setContent {
            val sharedPref = getSharedPreferences("settings", MODE_PRIVATE)

            var isDarkTheme by rememberSaveable {
                mutableStateOf(sharedPref.getBoolean("dark_theme", true))
            }

            ModernTorneoYaTheme(useDarkTheme = isDarkTheme) {
                val navController = rememberNavController()
                val context = this@MainActivity
                val owner = LocalViewModelStoreOwner.current ?: error("No ViewModelStoreOwner")

                val db = AppDatabase.getInstance(context)
                val usuarioLocalRepository = UsuarioLocalRepository(db.usuarioLocalDao())
                val jugadorRepository = JugadorRepository(db.jugadorDao())
                val partidoEquipoJugadorRepository = PartidoEquipoJugadorRepository(db.partidoEquipoJugadorDao())

                val partidoRepository = PartidoRepository(
                    db.partidoDao(),
                    db.partidoEquipoJugadorDao(),
                    db.equipoDao(),
                    db.jugadorDao()
                )
                val equipoRepository = EquipoRepository(
                    db.equipoDao(),
                    db.partidoEquipoJugadorDao(),
                    db.jugadorDao()
                )

                val usuarioLocalViewModel = ViewModelProvider(
                    owner,
                    UsuarioLocalViewModelFactory(usuarioLocalRepository)
                )[UsuarioLocalViewModel::class.java]

                val partidoViewModel = ViewModelProvider(
                    owner,
                    PartidoViewModelFactory(partidoRepository)
                )[PartidoViewModel::class.java]

                val globalUserViewModel = ViewModelProvider(
                    this@MainActivity,
                    ViewModelProvider.AndroidViewModelFactory.getInstance(application)
                )[GlobalUserViewModel::class.java]

                val navBackStackEntry = navController.currentBackStackEntryAsState()
                val showBottomBar = when (navBackStackEntry.value?.destination?.route) {
                    BottomNavItem.Home.route,
                    BottomNavItem.Online.route,
                    BottomNavItem.Amigos.route,
                    BottomNavItem.Usuario.route -> true
                    "partido_online" -> true
                    "visualizar_partido_online/{partidoUid}" -> false
                    else -> false
                }

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavigationBar(
                                navController, modifier = Modifier.navigationBarsPadding(),
                                isDarkTheme = isDarkTheme
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        NavGraph(
                            navController = navController,
                            usuarioLocalViewModel = usuarioLocalViewModel,
                            partidoViewModel = partidoViewModel,
                            partidoRepository = partidoRepository,
                            equipoRepository = equipoRepository,
                            globalUserViewModel = globalUserViewModel,
                            homeViewModel = homeViewModel,
                            isDarkTheme = isDarkTheme,
                            onThemeChange = { newValue ->
                                isDarkTheme = newValue
                                sharedPref.edit().putBoolean("dark_theme", newValue).apply()
                            }
                        )
                    }
                }
            }
        }
    }

    private fun updateLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
