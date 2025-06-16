package mingosgit.josecr.torneoya.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
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

@Composable
fun NavGraph(
    navController: NavHostController,
    usuarioLocalViewModel: UsuarioLocalViewModel,
    partidoViewModel: PartidoViewModel,
    partidoRepository: PartidoRepository
) {
    val owner = LocalViewModelStoreOwner.current ?: error("No ViewModelStoreOwner")
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
            val createPartidoViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                modelClass = CreatePartidoViewModel::class.java,
                viewModelStoreOwner = owner,
                factory = CreatePartidoViewModelFactory(partidoRepository)
            )

            CreatePartidoScreen(
                navController = navController,
                createPartidoViewModel = createPartidoViewModel
            )
        }
        // Placeholder para la siguiente screen de asignación de jugadores:
        composable("asignar_jugadores/{partidoId}") { backStackEntry ->
            // Aquí meterás tu pantalla de asignar jugadores usando el partidoId
            // val partidoId = backStackEntry.arguments?.getString("partidoId")?.toLongOrNull()
        }
    }
}
