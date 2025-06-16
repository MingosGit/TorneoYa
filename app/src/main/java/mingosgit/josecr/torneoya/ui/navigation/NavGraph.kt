package mingosgit.josecr.torneoya.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import mingosgit.josecr.torneoya.ui.screens.AsignarJugadoresScreen
import mingosgit.josecr.torneoya.viewmodel.AsignarJugadoresViewModel
import mingosgit.josecr.torneoya.repository.JugadorRepository
import androidx.lifecycle.viewmodel.compose.viewModel
import mingosgit.josecr.torneoya.data.database.AppDatabase

import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import mingosgit.josecr.torneoya.ui.screens.HomeScreen
import mingosgit.josecr.torneoya.ui.screens.PartidoScreen
import mingosgit.josecr.torneoya.ui.screens.UsuarioScreen
import mingosgit.josecr.torneoya.ui.screens.CreatePartidoScreen
import mingosgit.josecr.torneoya.viewmodel.PartidoViewModel
import mingosgit.josecr.torneoya.viewmodel.UsuarioLocalViewModel
import mingosgit.josecr.torneoya.viewmodel.CreatePartidoViewModel
import mingosgit.josecr.torneoya.viewmodel.CreatePartidoViewModelFactory
import mingosgit.josecr.torneoya.repository.PartidoRepository
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
        composable("asignar_jugadores/{partidoId}") { backStackEntry ->
            val partidoId = backStackEntry.arguments?.getString("partidoId")?.toLongOrNull() ?: return@composable
            // Recupera número de jugadores por partido (esto normalmente lo tendrías que pasar también en la navegación o sacar de la BD)
            // Aquí, por simplicidad, usamos el último partido cargado:
            val numJugadores = partidoViewModel.partidos.value.find { it.id == partidoId }?.numeroJugadores ?: 5

            val vm = viewModel(
                modelClass = AsignarJugadoresViewModel::class.java,
                viewModelStoreOwner = owner,
                factory = AsignarJugadoresViewModelFactory(
                    partidoId,
                    numJugadores,
                    jugadorRepository,
                    partidoRepository
                )
            )
            AsignarJugadoresScreen(
                navController = navController,
                vm = vm
            )
        }
    }
}

