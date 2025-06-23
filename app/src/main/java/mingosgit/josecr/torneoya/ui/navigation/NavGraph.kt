package mingosgit.josecr.torneoya.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import mingosgit.josecr.torneoya.ui.screens.partido.AsignarJugadoresScreen
import mingosgit.josecr.torneoya.viewmodel.partido.AsignarJugadoresViewModelFactory
import mingosgit.josecr.torneoya.viewmodel.partido.EditPartidoViewModelFactory
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
import kotlinx.coroutines.runBlocking
import mingosgit.josecr.torneoya.ui.screens.home.HomeScreen
import mingosgit.josecr.torneoya.ui.screens.partido.PartidoScreen
import mingosgit.josecr.torneoya.ui.screens.usuario.UsuarioScreen
import mingosgit.josecr.torneoya.ui.screens.partido.CreatePartidoScreen
import mingosgit.josecr.torneoya.ui.screens.partido.EditarJugadoresEquipoScreen
import mingosgit.josecr.torneoya.ui.screens.partido.EditPartidoScreen
import mingosgit.josecr.torneoya.ui.screens.partido.VisualizarPartidoScreen
import mingosgit.josecr.torneoya.viewmodel.partido.AsignarJugadoresViewModel
import mingosgit.josecr.torneoya.viewmodel.partido.PartidoViewModel
import mingosgit.josecr.torneoya.viewmodel.usuario.UsuarioLocalViewModel
import mingosgit.josecr.torneoya.viewmodel.partido.CreatePartidoViewModelFactory
import mingosgit.josecr.torneoya.viewmodel.partido.VisualizarPartidoViewModelFactory
import mingosgit.josecr.torneoya.viewmodel.partido.EditarJugadoresEquipoViewModel
import mingosgit.josecr.torneoya.viewmodel.partido.EditarJugadoresEquipoViewModelFactory
import mingosgit.josecr.torneoya.viewmodel.partido.CreatePartidoViewModel
import mingosgit.josecr.torneoya.viewmodel.partido.EditPartidoViewModel
import mingosgit.josecr.torneoya.viewmodel.partido.VisualizarPartidoViewModel

import mingosgit.josecr.torneoya.repository.ComentarioRepository
import mingosgit.josecr.torneoya.repository.EncuestaRepository
import mingosgit.josecr.torneoya.repository.UsuarioLocalRepository

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
    val equipoRepositoryInst = EquipoRepository(
        equipoDao = db.equipoDao(),
        partidoEquipoJugadorDao = db.partidoEquipoJugadorDao(),
        jugadorDao = db.jugadorDao()
    )
    val usuarioLocalRepository = UsuarioLocalRepository(db.usuarioLocalDao())
    val jugadorRepositoryInst = JugadorRepository(db.jugadorDao())
    val relacionRepositoryInst = PartidoEquipoJugadorRepository(db.partidoEquipoJugadorDao())
    val comentarioRepository = ComentarioRepository(db.comentarioDao())
    val encuestaRepository = EncuestaRepository(db.encuestaDao(), db.encuestaVotoDao())

    val usuarioId = runBlocking {
        usuarioLocalRepository.getUsuario()?.id ?: 0L
    }

    NavHost(navController, startDestination = BottomNavItem.Home.route) {
        composable(BottomNavItem.Home.route) {
            HomeScreen()
        }
        composable(BottomNavItem.Partido.route) {
            PartidoScreen(
                navController = navController,
                partidoViewModel = partidoViewModel,
                equipoRepository = equipoRepositoryInst
            )
        }
        composable(BottomNavItem.Usuario.route) {
            UsuarioScreen(usuarioLocalViewModel)
        }
        composable("crear_partido") {
            val createPartidoViewModel = viewModel(
                modelClass = CreatePartidoViewModel::class.java,
                viewModelStoreOwner = owner,
                factory = CreatePartidoViewModelFactory(partidoRepository, equipoRepositoryInst)
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

            // FRAGMENTO IMPORTANTE
            val usuarioId = runBlocking {
                usuarioLocalRepository.getUsuario()?.id ?: 0L
            }

            val visualizarPartidoViewModel = viewModel(
                modelClass = VisualizarPartidoViewModel::class.java,
                viewModelStoreOwner = owner,
                factory = VisualizarPartidoViewModelFactory(
                    partidoId = id,
                    partidoRepository = partidoRepository,
                    equipoRepository = equipoRepositoryInst,
                    comentarioRepository = comentarioRepository,
                    encuestaRepository = encuestaRepository
                ),
                key = "visualizar_partido_$id"
            )
            VisualizarPartidoScreen(
                partidoId = id,
                navController = navController,
                vm = visualizarPartidoViewModel,
                usuarioId = usuarioId
            )
        }
        composable(
            "editar_partido/{partidoId}",
            arguments = listOf(navArgument("partidoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("partidoId") ?: return@composable
            val uniqueKey = "editar_partido_${id}_${System.currentTimeMillis()}"
            val editPartidoViewModel = viewModel(
                modelClass = EditPartidoViewModel::class.java,
                viewModelStoreOwner = owner,
                factory = EditPartidoViewModelFactory(
                    partidoRepository = partidoRepository,
                    jugadorRepository = jugadorRepositoryInst,
                    equipoRepository = equipoRepositoryInst,
                    partidoId = id
                ),
                key = uniqueKey
            )
            EditPartidoScreen(
                partidoId = id,
                navController = navController,
                editPartidoViewModel = editPartidoViewModel,
                onFinish = {
                    navController.previousBackStackEntry?.arguments?.putBoolean(
                        "reload_partidos",
                        true
                    )
                    navController.previousBackStackEntry?.arguments?.putBoolean(
                        "reload_partido",
                        true
                    )
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
                modelClass = AsignarJugadoresViewModel::class.java,
                viewModelStoreOwner = owner,
                factory = AsignarJugadoresViewModelFactory(
                    partidoId,
                    numJugadores,
                    equipoAId,
                    equipoBId,
                    jugadorRepositoryInst,
                    partidoRepository,
                    relacionRepositoryInst
                ),
                key = "asignar_jugadores_${partidoId}"
            )
            AsignarJugadoresScreen(
                navController = navController,
                vm = vm
            )
        }
        composable(
            route = "editar_jugadores/{partidoId}?equipoAId={equipoAId}&equipoBId={equipoBId}",
            arguments = listOf(
                navArgument("partidoId") { type = NavType.LongType },
                navArgument("equipoAId") { type = NavType.LongType; defaultValue = -1L },
                navArgument("equipoBId") { type = NavType.LongType; defaultValue = -1L }
            )
        ) { backStackEntry ->
            val partidoId = backStackEntry.arguments?.getLong("partidoId") ?: return@composable
            val equipoAId = backStackEntry.arguments?.getLong("equipoAId") ?: -1L
            val equipoBId = backStackEntry.arguments?.getLong("equipoBId") ?: -1L
            val vm = viewModel(
                modelClass = EditarJugadoresEquipoViewModel::class.java,
                viewModelStoreOwner = owner,
                factory = EditarJugadoresEquipoViewModelFactory(
                    partidoId,
                    equipoAId,
                    equipoBId,
                    jugadorRepositoryInst,
                    relacionRepositoryInst,
                    equipoRepositoryInst
                ),
                key = "editar_jugadores_${partidoId}_${equipoAId}_${equipoBId}_${System.currentTimeMillis()}"
            )
            EditarJugadoresEquipoScreen(
                partidoId = partidoId,
                equipoAId = equipoAId,
                equipoBId = equipoBId,
                navController = navController,
                vm = vm
            )
        }
    }
}
