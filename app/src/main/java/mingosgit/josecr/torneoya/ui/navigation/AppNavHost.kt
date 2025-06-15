package mingosgit.josecr.torneoya.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import mingosgit.josecr.torneoya.data.entities.PartidoEntity
import mingosgit.josecr.torneoya.ui.screens.CrearPartidoScreen
import mingosgit.josecr.torneoya.ui.screens.EditarPartidoScreen
import mingosgit.josecr.torneoya.ui.screens.HomeScreen
import mingosgit.josecr.torneoya.viewmodel.AppViewModelFactory
import mingosgit.josecr.torneoya.viewmodel.HomeViewModel
import mingosgit.josecr.torneoya.viewmodel.PartidoViewModel

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {

        // HOME
        composable("home") {
            val context = LocalContext.current.applicationContext
            val factory = remember { AppViewModelFactory(context) }
            val viewModel: HomeViewModel = viewModel(
                modelClass = HomeViewModel::class.java,
                factory = factory
            )

            // Recarga datos al volver a la Home (por ejemplo, tras crear un partido)
            LaunchedEffect(Unit, navController.currentBackStackEntry) {
                viewModel.cargarDatos()
            }

            HomeScreen(
                onCrearPartido = { navController.navigate("crearPartido") },
                onTorneoClick = { /* Puedes agregar navegación a editar torneo aquí */ },
                onPartidoClick = { partidoId -> navController.navigate("editarPartido/$partidoId") }
            )
        }

        // AGREGADO: RUTA PARA CREAR PARTIDO
        composable("crearPartido") {
            val context = LocalContext.current.applicationContext
            val factory = remember { AppViewModelFactory(context) }
            val partidoViewModel: PartidoViewModel = viewModel(
                modelClass = PartidoViewModel::class.java,
                factory = factory
            )

            CrearPartidoScreen { equipos, tiempo ->
                // Ejemplo para el primer equipo y jugador
                if (equipos.size >= 2 && equipos[0].isNotEmpty() && equipos[1].isNotEmpty()) {
                    val partido = PartidoEntity(
                        equipoLocalId = 1L,
                        equipoVisitanteId = 2L,
                        fecha = System.currentTimeMillis()
                    )
                    partidoViewModel.guardarPartido(partido) {
                        navController.popBackStack()
                    }
                } else {
                    navController.popBackStack()
                }
            }
        }


        // EDITAR PARTIDO
        composable(
            "editarPartido/{partidoId}",
            arguments = listOf(navArgument("partidoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val partidoId = backStackEntry.arguments?.getLong("partidoId") ?: 0L
            EditarPartidoScreen(
                partidoId = partidoId,
                onPartidoEditado = { navController.popBackStack() }
            )
        }
    }
}
