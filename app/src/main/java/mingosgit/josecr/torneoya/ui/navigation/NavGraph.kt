package mingosgit.josecr.torneoya.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.coroutines.runBlocking
import mingosgit.josecr.torneoya.data.database.AppDatabase
import mingosgit.josecr.torneoya.repository.*
import mingosgit.josecr.torneoya.ui.screens.amigos.AmigosScreen
import mingosgit.josecr.torneoya.ui.screens.home.HomeScreen
import mingosgit.josecr.torneoya.ui.screens.home.HomeViewModel
import mingosgit.josecr.torneoya.ui.screens.partido.*
import mingosgit.josecr.torneoya.viewmodel.partido.*
import mingosgit.josecr.torneoya.viewmodel.usuario.UsuarioLocalViewModel
import mingosgit.josecr.torneoya.viewmodel.usuario.GlobalUserViewModel

import mingosgit.josecr.torneoya.ui.screens.amigos.SolicitudesPendientesScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    usuarioLocalViewModel: UsuarioLocalViewModel,
    partidoViewModel: PartidoViewModel,
    partidoRepository: mingosgit.josecr.torneoya.repository.PartidoRepository,
    equipoRepository: EquipoRepository,
    globalUserViewModel: GlobalUserViewModel = viewModel()
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
    val comentarioRepository = ComentarioRepository(
        comentarioDao = db.comentarioDao(),
        comentarioVotoDao = db.comentarioVotoDao()
    )
    val encuestaRepository = EncuestaRepository(db.encuestaDao(), db.encuestaVotoDao())
    val goleadorRepository = GoleadorRepository(db.goleadorDao())
    val eventoRepository = EventoRepository(db.eventoDao())
    val equipoPredefinidoRepository = EquipoPredefinidoRepository(db.equipoPredefinidoDao())

    NavHost(navController, startDestination = BottomNavItem.Home.route) {
        composable(BottomNavItem.Home.route) {
            val homeViewModel = viewModel<HomeViewModel>(
                factory = HomeViewModel.Factory(
                    usuarioLocalRepository,
                    partidoRepository,
                    equipoRepositoryInst,
                    jugadorRepositoryInst
                )
            )
            HomeScreen(viewModel = homeViewModel)
        }
        composable(BottomNavItem.Partido.route) {
            PartidoScreen(
                navController = navController,
                partidoViewModel = partidoViewModel,
                equipoRepository = equipoRepositoryInst
            )
        }
        composable(BottomNavItem.Amigos.route) {
            BackHandler {
                navController.navigate(BottomNavItem.Home.route) {
                    popUpTo(BottomNavItem.Home.route) { inclusive = false }
                    launchSingleTop = true
                }
            }
            AmigosScreen(
                navController = navController,
                globalUserViewModel = globalUserViewModel
            )
        }
        composable("solicitudes_pendientes") {
            SolicitudesPendientesScreen(navController = navController)
        }

        // Modularizado
        addUsuarioModuleNavGraph(
            navController = navController,
            owner = owner,
            usuarioLocalViewModel = usuarioLocalViewModel,
            globalUserViewModel = globalUserViewModel,
            db = db,
            partidoRepository = partidoRepository,
            equipoRepositoryInst = equipoRepositoryInst,
            usuarioLocalRepository = usuarioLocalRepository,
            jugadorRepositoryInst = jugadorRepositoryInst,
            eventoRepository = eventoRepository,
            goleadorRepository = goleadorRepository,
            comentarioRepository = comentarioRepository,
            encuestaRepository = encuestaRepository,
            relacionRepositoryInst = relacionRepositoryInst,
            equipoPredefinidoRepository = equipoPredefinidoRepository
        )

        composable("crear_partido") {
            val createPartidoViewModel = viewModel(
                modelClass = CreatePartidoViewModel::class.java,
                viewModelStoreOwner = owner,
                factory = CreatePartidoViewModelFactory(partidoRepository, equipoRepositoryInst)
            )
            val equiposPredefinidosVM = viewModel<mingosgit.josecr.torneoya.viewmodel.equipopredefinido.EquiposPredefinidosViewModel>(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return mingosgit.josecr.torneoya.viewmodel.equipopredefinido.EquiposPredefinidosViewModel(
                            equipoPredefinidoRepository
                        ) as T
                    }
                }
            )
            CreatePartidoScreen(
                navController = navController,
                createPartidoViewModel = createPartidoViewModel,
                equiposPredefinidosViewModel = equiposPredefinidosVM
            )
        }
        composable(
            "visualizar_partido/{partidoId}",
            arguments = listOf(androidx.navigation.navArgument("partidoId") { type = androidx.navigation.NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("partidoId") ?: return@composable

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
                usuarioId = usuarioId,
                eventoRepository = eventoRepository,
                jugadorRepository = jugadorRepositoryInst,
                equipoRepository = equipoRepositoryInst
            )
        }
        composable(
            "editar_partido/{partidoId}",
            arguments = listOf(androidx.navigation.navArgument("partidoId") { type = androidx.navigation.NavType.LongType })
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
            route = "asignar_jugadores/{partidoId}?equipoAId={equipoAId}&equipoBId={equipoBId}&fecha={fecha}&horaInicio={horaInicio}&numeroPartes={numeroPartes}&tiempoPorParte={tiempoPorParte}&tiempoDescanso={tiempoDescanso}&numeroJugadores={numeroJugadores}&equipoAPredefinidoId={equipoAPredefinidoId}&equipoBPredefinidoId={equipoBPredefinidoId}",
            arguments = listOf(
                androidx.navigation.navArgument("partidoId") { type = androidx.navigation.NavType.LongType },
                androidx.navigation.navArgument("equipoAId") { type = androidx.navigation.NavType.LongType; defaultValue = -1L },
                androidx.navigation.navArgument("equipoBId") { type = androidx.navigation.NavType.LongType; defaultValue = -1L },
                androidx.navigation.navArgument("fecha") { defaultValue = "" },
                androidx.navigation.navArgument("horaInicio") { defaultValue = "" },
                androidx.navigation.navArgument("numeroPartes") { defaultValue = "2" },
                androidx.navigation.navArgument("tiempoPorParte") { defaultValue = "25" },
                androidx.navigation.navArgument("tiempoDescanso") { defaultValue = "5" },
                androidx.navigation.navArgument("numeroJugadores") { defaultValue = "5" },
                androidx.navigation.navArgument("equipoAPredefinidoId") { type = androidx.navigation.NavType.LongType; defaultValue = -1L },
                androidx.navigation.navArgument("equipoBPredefinidoId") { type = androidx.navigation.NavType.LongType; defaultValue = -1L }
            )
        ) { backStackEntry ->
            val partidoId = backStackEntry.arguments?.getLong("partidoId") ?: return@composable
            val equipoAId = backStackEntry.arguments?.getLong("equipoAId") ?: -1L
            val equipoBId = backStackEntry.arguments?.getLong("equipoBId") ?: -1L
            val numJugadores = backStackEntry.arguments?.getString("numeroJugadores")?.toIntOrNull() ?: 5
            val equipoAPredefinidoId = backStackEntry.arguments?.getLong("equipoAPredefinidoId")?.takeIf { it > 0 }
            val equipoBPredefinidoId = backStackEntry.arguments?.getLong("equipoBPredefinidoId")?.takeIf { it > 0 }
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
                    relacionRepositoryInst,
                    equipoPredefinidoRepository,
                    equipoAPredefinidoId,
                    equipoBPredefinidoId
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
                androidx.navigation.navArgument("partidoId") { type = androidx.navigation.NavType.LongType },
                androidx.navigation.navArgument("equipoAId") { type = androidx.navigation.NavType.LongType; defaultValue = -1L },
                androidx.navigation.navArgument("equipoBId") { type = androidx.navigation.NavType.LongType; defaultValue = -1L }
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
