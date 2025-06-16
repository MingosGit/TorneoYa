package mingosgit.josecr.torneoya.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import mingosgit.josecr.torneoya.ui.screens.HomeScreen
import mingosgit.josecr.torneoya.ui.screens.PartidoScreen
import mingosgit.josecr.torneoya.ui.screens.UsuarioScreen
import mingosgit.josecr.torneoya.viewmodel.UsuarioLocalViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    usuarioLocalViewModel: UsuarioLocalViewModel
) {
    NavHost(navController, startDestination = BottomNavItem.Home.route) {
        composable(BottomNavItem.Home.route) { HomeScreen() }
        composable(BottomNavItem.Partido.route) { PartidoScreen() }
        composable(BottomNavItem.Usuario.route) {
            UsuarioScreen(usuarioLocalViewModel)
        }
    }
}
