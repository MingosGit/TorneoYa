package mingosgit.josecr.torneoya

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import mingosgit.josecr.torneoya.data.database.AppDatabase
import mingosgit.josecr.torneoya.repository.UsuarioLocalRepository
import mingosgit.josecr.torneoya.repository.PartidoRepository
import mingosgit.josecr.torneoya.repository.EquipoRepository
import mingosgit.josecr.torneoya.repository.JugadorRepository
import mingosgit.josecr.torneoya.repository.PartidoEquipoJugadorRepository
import mingosgit.josecr.torneoya.ui.navigation.BottomNavigationBar
import mingosgit.josecr.torneoya.ui.navigation.NavGraph
import mingosgit.josecr.torneoya.ui.navigation.BottomNavItem
import mingosgit.josecr.torneoya.ui.theme.TorneoYaTheme
import mingosgit.josecr.torneoya.viewmodel.usuario.UsuarioLocalViewModel
import mingosgit.josecr.torneoya.viewmodel.usuario.UsuarioLocalViewModelFactory
import mingosgit.josecr.torneoya.viewmodel.partido.PartidoViewModel
import mingosgit.josecr.torneoya.viewmodel.partido.PartidoViewModelFactory
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.graphics.Color as AndroidColor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // COLOR DE LA BARRA DE ESTADO NEGRA (AUNQUE DÉ WARNING)
        window.statusBarColor = AndroidColor.BLACK

        // ICONOS DE LA BARRA SIEMPRE BLANCOS (APARIENCIA OSCURA)
        WindowCompat.getInsetsController(window, window.decorView)?.isAppearanceLightStatusBars = false

        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContent {
            TorneoYaTheme {
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

                val navBackStackEntry = navController.currentBackStackEntryAsState()
                val showBottomBar = when (navBackStackEntry.value?.destination?.route) {
                    BottomNavItem.Home.route,
                    BottomNavItem.Partido.route,
                    BottomNavItem.Online.route,
                    BottomNavItem.Usuario.route -> true
                    "partido_online" -> true
                    // AÑADE ESTO:
                    "visualizar_partido_online/{partidoUid}" -> false
                    // o simplemente ignora esa ruta para ocultar navbar
                    else -> false
                }


                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavigationBar(navController)
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
                            equipoRepository = equipoRepository
                        )
                    }
                }
            }
        }
    }
}
