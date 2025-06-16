package mingosgit.josecr.torneoya.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import mingosgit.josecr.torneoya.ui.screens.AsignarJugadoresScreen
import mingosgit.josecr.torneoya.viewmodel.AsignarJugadoresViewModel
import mingosgit.josecr.torneoya.repository.JugadorRepository
import androidx.lifecycle.viewmodel.compose.viewModel
import mingosgit.josecr.torneoya.data.database.AppDatabase
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import mingosgit.josecr.torneoya.ui.screens.HomeScreen
import mingosgit.josecr.torneoya.ui.screens.PartidoScreen
import mingosgit.josecr.torneoya.ui.screens.UsuarioScreen
import mingosgit.josecr.torneoya.ui.screens.CreatePartidoScreen
import mingosgit.josecr.torneoya.viewmodel.PartidoViewModel
import mingosgit.josecr.torneoya.viewmodel.UsuarioLocalViewModel
import mingosgit.josecr.torneoya.viewmodel.CreatePartidoViewModel
import mingosgit.josecr.torneoya.viewmodel.CreatePartidoViewModelFactory
import mingosgit.josecr.torneoya.repository.PartidoRepository
import mingosgit.josecr.torneoya.ui.screens.EditPartidoScreen
import mingosgit.josecr.torneoya.viewmodel.AsignarJugadoresViewModelFactory

@Composable
fun NavGraph(
    navController: NavHostController,
    usuarioLocalViewModel: UsuarioLocalViewModel,
    partidoViewModel: PartidoViewModel,
    partidoRepository: PartidoRepository
) {
    val owner = LocalViewModelStoreOwner.current ?: error("No ViewModelStoreOwner")
    val context = LocalContext.current
    val db = AppDatabase.getInstance(context)
    val jugadorRepository = JugadorRepository(db.jugadorDao())

    NavHost(navController, startDestination = BottomNavItem.Home.route) {
        composable(BottomNavItem.Home.route) { HomeScreen() }
        composable(BottomNavItem.Partido.route) {
            PartidoScreen(
                navController = navController,
                partidoViewModel = partidoViewModel
            )
        }
        composable(BottomNavItem.Usuario.route) {
            UsuarioScreen(usuarioLocalViewModel)
        }
        composable("crear_partido") {
            val createPartidoViewModel = viewModel(
                modelClass = CreatePartidoViewModel::class.java,
                viewModelStoreOwner = owner,
                factory = CreatePartidoViewModelFactory(partidoRepository)
            )
            CreatePartidoScreen(
                navController = navController,
                createPartidoViewModel = createPartidoViewModel
            )
        }
        composable(
            "editar_partido/{partidoId}",
            arguments = listOf(navArgument("partidoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("partidoId") ?: return@composable
            EditPartidoScreen(
                partidoId = id,
                navController = navController,
                partidoRepository = partidoRepository
            )
        }
        composable(
            route = "asignar_jugadores/{partidoId}?equipoA={equipoA}&equipoB={equipoB}&fecha={fecha}&horaInicio={horaInicio}&numeroPartes={numeroPartes}&tiempoPorParte={tiempoPorParte}&numeroJugadores={numeroJugadores}",
            arguments = listOf(
                navArgument("partidoId") { type = NavType.LongType },
                navArgument("equipoA") { defaultValue = "" },
                navArgument("equipoB") { defaultValue = "" },
                navArgument("fecha") { defaultValue = "" },
                navArgument("horaInicio") { defaultValue = "" },
                navArgument("numeroPartes") { defaultValue = "2" },
                navArgument("tiempoPorParte") { defaultValue = "25" },
                navArgument("numeroJugadores") { defaultValue = "5" }
            )
        ) { backStackEntry ->
            val partidoId = backStackEntry.arguments?.getLong("partidoId") ?: return@composable
            val numJugadores = backStackEntry.arguments?.getString("numeroJugadores")?.toIntOrNull() ?: 5
            val vm = viewModel(
                modelClass = mingosgit.josecr.torneoya.viewmodel.AsignarJugadoresViewModel::class.java,
                viewModelStoreOwner = owner,
                factory = mingosgit.josecr.torneoya.viewmodel.AsignarJugadoresViewModelFactory(
                    partidoId,
                    numJugadores,
                    jugadorRepository,
                    partidoRepository
                )
            )
            mingosgit.josecr.torneoya.ui.screens.AsignarJugadoresScreen(
                navController = navController,
                vm = vm
            )
        }
    }
}
