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
import mingosgit.josecr.torneoya.ui.screens.EditarIntegrantesScreen
import mingosgit.josecr.torneoya.ui.screens.HomeScreen
import mingosgit.josecr.torneoya.ui.screens.AgregarJugadoresScreen
import mingosgit.josecr.torneoya.viewmodel.AppViewModelFactory
import mingosgit.josecr.torneoya.viewmodel.HomeViewModel
import mingosgit.josecr.torneoya.viewmodel.PartidoViewModel

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current.applicationContext

    // Estados globales del flujo de creación
    var jugadores by remember { mutableStateOf(listOf<String>()) }
    var totalJugadoresNecesarios by remember { mutableStateOf(2) }
    var numIntegrantes by remember { mutableStateOf(2) }
    var numEquipos by remember { mutableStateOf(2) }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
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
                    jugadores = listOf()
                    totalJugadoresNecesarios = 2
                    numIntegrantes = 2
                    numEquipos = 2
                    navController.navigate("crearPartido")
                },
                onTorneoClick = { /* Puedes agregar navegación a editar torneo aquí */ },
                onPartidoClick = { partidoId -> navController.navigate("editarPartido/$partidoId") }
            )
        }
        composable("crearPartido") {
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
                    totalJugadoresNecesarios = numIntegrantes * numEquipos
                    navController.navigate("agregarJugadores")
                },
                onNumIntegrantesChange = {
                    numIntegrantes = it
                },
                onNumEquiposChange = {
                    numEquipos = it
                },
                numIntegrantes = numIntegrantes,
                numEquipos = numEquipos
            )
        }
        composable("agregarJugadores") {
            AgregarJugadoresScreen(
                jugadores = jugadores,
                onJugadoresListo = { nuevosJugadores -> jugadores = nuevosJugadores },
                navController = navController,
                totalNecesario = totalJugadoresNecesarios
            )
        }
        composable(
            "editarPartido/{partidoId}",
            arguments = listOf(navArgument("partidoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val partidoId = backStackEntry.arguments?.getLong("partidoId") ?: 0L
            EditarPartidoScreen(
                partidoId = partidoId,
                onPartidoEditado = { navController.popBackStack() },
                navController = navController
            )
        }
        composable(
            "editarIntegrantes/{partidoId}",
            arguments = listOf(navArgument("partidoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val partidoId = backStackEntry.arguments?.getLong("partidoId") ?: 0L
            EditarIntegrantesScreen(
                partidoId = partidoId,
                navController = navController
            )
        }
    }
}
