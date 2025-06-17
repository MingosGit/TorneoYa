package mingosgit.josecr.torneoya.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import mingosgit.josecr.torneoya.ui.screens.AsignarJugadoresScreen
import mingosgit.josecr.torneoya.viewmodel.AsignarJugadoresViewModelFactory
import mingosgit.josecr.torneoya.viewmodel.EditPartidoViewModelFactory
import mingosgit.josecr.torneoya.repository.JugadorRepository
import mingosgit.josecr.torneoya.repository.EquipoRepository
import mingosgit.josecr.torneoya.repository.PartidoEquipoJugadorRepository
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
import mingosgit.josecr.torneoya.viewmodel.CreatePartidoViewModelFactory
import mingosgit.josecr.torneoya.viewmodel.EditPartidoViewModel
import mingosgit.josecr.torneoya.viewmodel.CreatePartidoViewModel
import mingosgit.josecr.torneoya.viewmodel.VisualizarPartidoViewModelFactory
import mingosgit.josecr.torneoya.ui.screens.VisualizarPartidoScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    usuarioLocalViewModel: UsuarioLocalViewModel,
    partidoViewModel: PartidoViewModel,
    partidoRepository: mingosgit.josecr.torneoya.repository.PartidoRepository,
    equipoRepository: EquipoRepository
) {
    val owner = LocalViewModelStoreOwner.current ?: error("No ViewModelStoreOwner")
    val context = LocalContext.current
    val db = AppDatabase.getInstance(context)
    val equipoRepository = EquipoRepository(
        equipoDao = db.equipoDao(),
        partidoEquipoJugadorDao = db.partidoEquipoJugadorDao(),
        jugadorDao = db.jugadorDao()
    )
    val jugadorRepository = JugadorRepository(db.jugadorDao())
    val relacionRepository = PartidoEquipoJugadorRepository(db.partidoEquipoJugadorDao())

    NavHost(navController, startDestination = BottomNavItem.Home.route) {
        composable(BottomNavItem.Home.route) {
            HomeScreen()
        }
        composable(BottomNavItem.Partido.route) {
            PartidoScreen(
                navController = navController,
                partidoViewModel = partidoViewModel,
                equipoRepository = equipoRepository
            )
        }
        composable(BottomNavItem.Usuario.route) {
            UsuarioScreen(usuarioLocalViewModel)
        }
        composable("crear_partido") {
            val createPartidoViewModel = viewModel(
                modelClass = mingosgit.josecr.torneoya.viewmodel.CreatePartidoViewModel::class.java,
                viewModelStoreOwner = owner,
                factory = CreatePartidoViewModelFactory(partidoRepository, equipoRepository)
            )
            CreatePartidoScreen(
                navController = navController,
                createPartidoViewModel = createPartidoViewModel
            )
        }
        composable(
            "visualizar_partido/{partidoId}",
            arguments = listOf(navArgument("partidoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("partidoId") ?: return@composable
            val visualizarPartidoViewModel = viewModel(
                modelClass = mingosgit.josecr.torneoya.viewmodel.VisualizarPartidoViewModel::class.java,
                viewModelStoreOwner = owner,
                factory = VisualizarPartidoViewModelFactory(
                    partidoId = id,
                    partidoRepository = partidoRepository,
                    equipoRepository = equipoRepository
                ),
                key = "visualizar_partido_$id"
            )
            mingosgit.josecr.torneoya.ui.screens.VisualizarPartidoScreen(
                partidoId = id,
                navController = navController,
                vm = visualizarPartidoViewModel
            )
        }
        composable(
            "editar_partido/{partidoId}",
            arguments = listOf(navArgument("partidoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("partidoId") ?: return@composable

            // CAMBIO CLAVE: Usar key dinámico basado en id+timestamp para forzar ViewModel NUEVO cada vez
            val uniqueKey = "editar_partido_${id}_${System.currentTimeMillis()}"
            val editPartidoViewModel = viewModel(
                modelClass = mingosgit.josecr.torneoya.viewmodel.EditPartidoViewModel::class.java,
                viewModelStoreOwner = owner,
                factory = EditPartidoViewModelFactory(
                    partidoRepository = partidoRepository,
                    jugadorRepository = jugadorRepository,
                    equipoRepository = equipoRepository,
                    partidoId = id
                ),
                key = uniqueKey // <-- Aquí está el truco
            )
            mingosgit.josecr.torneoya.ui.screens.EditPartidoScreen(
                partidoId = id,
                navController = navController,
                editPartidoViewModel = editPartidoViewModel,
                onFinish = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("reload_partidos", true)
                }
            )
        }
        composable(
            route = "asignar_jugadores/{partidoId}?equipoAId={equipoAId}&equipoBId={equipoBId}&fecha={fecha}&horaInicio={horaInicio}&numeroPartes={numeroPartes}&tiempoPorParte={tiempoPorParte}&numeroJugadores={numeroJugadores}",
            arguments = listOf(
                navArgument("partidoId") { type = NavType.LongType },
                navArgument("equipoAId") { type = NavType.LongType; defaultValue = -1L },
                navArgument("equipoBId") { type = NavType.LongType; defaultValue = -1L },
                navArgument("fecha") { defaultValue = "" },
                navArgument("horaInicio") { defaultValue = "" },
                navArgument("numeroPartes") { defaultValue = "2" },
                navArgument("tiempoPorParte") { defaultValue = "25" },
                navArgument("numeroJugadores") { defaultValue = "5" }
            )
        ) { backStackEntry ->
            val partidoId = backStackEntry.arguments?.getLong("partidoId") ?: return@composable
            val equipoAId = backStackEntry.arguments?.getLong("equipoAId") ?: -1L
            val equipoBId = backStackEntry.arguments?.getLong("equipoBId") ?: -1L
            val numJugadores = backStackEntry.arguments?.getString("numeroJugadores")?.toIntOrNull() ?: 5
            val vm = viewModel(
                modelClass = mingosgit.josecr.torneoya.viewmodel.AsignarJugadoresViewModel::class.java,
                viewModelStoreOwner = owner,
                factory = AsignarJugadoresViewModelFactory(
                    partidoId,
                    numJugadores,
                    equipoAId,
                    equipoBId,
                    jugadorRepository,
                    partidoRepository,
                    relacionRepository
                ),
                key = "asignar_jugadores_${partidoId}"
            )
            AsignarJugadoresScreen(
                navController = navController,
                vm = vm
            )
        }
    }
}
