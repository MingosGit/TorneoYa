package mingosgit.josecr.torneoya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import mingosgit.josecr.torneoya.ui.theme.ModernTorneoYaTheme
import mingosgit.josecr.torneoya.viewmodel.usuario.UsuarioLocalViewModel
import mingosgit.josecr.torneoya.viewmodel.usuario.UsuarioLocalViewModelFactory
import mingosgit.josecr.torneoya.viewmodel.partido.PartidoViewModel
import mingosgit.josecr.torneoya.viewmodel.partido.PartidoViewModelFactory
import mingosgit.josecr.torneoya.viewmodel.usuario.GlobalUserViewModel
import mingosgit.josecr.torneoya.ui.screens.home.HomeViewModel
import androidx.core.view.WindowCompat
import android.graphics.Color as AndroidColor

class MainActivity : ComponentActivity() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_TorneoYa)
        super.onCreate(savedInstanceState)

        window.statusBarColor = AndroidColor.BLACK
        window.navigationBarColor = AndroidColor.BLACK
        WindowCompat.getInsetsController(window, window.decorView)?.isAppearanceLightStatusBars = false
        WindowCompat.setDecorFitsSystemWindows(window, false)

        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        setContent {
            ModernTorneoYaTheme {
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

                androidx.compose.material3.Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavigationBar(navController, modifier = Modifier.navigationBarsPadding())
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
                            homeViewModel = homeViewModel
                        )
                    }
                }
            }
        }
    }
}
