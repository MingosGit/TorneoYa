package mingosgit.josecr.torneoya.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import mingosgit.josecr.torneoya.ui.screens.CrearPartidoScreen
import mingosgit.josecr.torneoya.ui.screens.EditarPartidoScreen
import mingosgit.josecr.torneoya.ui.screens.HomeScreen
import mingosgit.josecr.torneoya.ui.screens.AgregarJugadoresScreen
import mingosgit.josecr.torneoya.viewmodel.AppViewModelFactory
import mingosgit.josecr.torneoya.viewmodel.HomeViewModel
import mingosgit.josecr.torneoya.viewmodel.PartidoViewModel

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    // Estado para la lista de jugadores seleccionados durante el flujo de creación de partido
    var jugadores by remember { mutableStateOf(listOf<String>()) }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            val context = LocalContext.current.applicationContext
            val factory = remember { AppViewModelFactory(context) }
            val viewModel: HomeViewModel = viewModel(
                modelClass = HomeViewModel::class.java,
                factory = factory
            )
            LaunchedEffect(Unit, navController.currentBackStackEntry) {
                viewModel.cargarDatos()
            }
            HomeScreen(
                onCrearPartido = {
                    jugadores = listOf() // Reinicia la lista de jugadores al crear partido
                    navController.navigate("crearPartido")
                },
                onTorneoClick = { /* Puedes agregar navegación a editar torneo aquí */ },
                onPartidoClick = { partidoId -> navController.navigate("editarPartido/$partidoId") }
            )
        }
        composable("crearPartido") {
            val context = LocalContext.current.applicationContext
            val factory = remember { AppViewModelFactory(context) }
            val partidoViewModel: PartidoViewModel = viewModel(
                modelClass = PartidoViewModel::class.java,
                factory = factory
            )
            CrearPartidoScreen(
                onPartidoCreado = { equipos, tiempo ->
                    if (equipos.size >= 2 && equipos[0].isNotEmpty() && equipos[1].isNotEmpty()) {
                        val nombreEquipoLocal = "Equipo 1"
                        val nombreEquipoVisitante = "Equipo 2"
                        val nombresIntegrantesLocal = equipos[0]
                        val nombresIntegrantesVisitante = equipos[1]
                        partidoViewModel.crearPartidoConIntegrantes(
                            nombreEquipoLocal = nombreEquipoLocal,
                            nombresIntegrantesLocal = nombresIntegrantesLocal,
                            nombreEquipoVisitante = nombreEquipoVisitante,
                            nombresIntegrantesVisitante = nombresIntegrantesVisitante,
                            fecha = System.currentTimeMillis()
                        ) {
                            navController.popBackStack()
                        }
                    } else {
                        navController.popBackStack()
                    }
                },
                jugadores = jugadores,
                onAgregarJugadores = {
                    navController.navigate("agregarJugadores")
                }
            )
        }
        composable("agregarJugadores") {
            AgregarJugadoresScreen(
                jugadores = jugadores,
                onJugadoresListo = { nuevosJugadores ->
                    jugadores = nuevosJugadores
                },
                navController = navController
            )
        }
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
