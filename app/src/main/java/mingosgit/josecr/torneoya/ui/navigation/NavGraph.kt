package mingosgit.josecr.torneoya.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kotlinx.coroutines.runBlocking
import mingosgit.josecr.torneoya.data.database.AppDatabase
import mingosgit.josecr.torneoya.repository.*
import mingosgit.josecr.torneoya.ui.screens.amigos.AmigosScreen
import mingosgit.josecr.torneoya.ui.screens.home.HomeScreen
import mingosgit.josecr.torneoya.ui.screens.home.HomeViewModel
import mingosgit.josecr.torneoya.ui.screens.partido.*
import mingosgit.josecr.torneoya.ui.screens.usuario.*
import mingosgit.josecr.torneoya.ui.screens.equipopredefinido.EquiposPredefinidosScreen
import mingosgit.josecr.torneoya.ui.screens.equipopredefinido.CrearEquipoPredefinidoScreen
import mingosgit.josecr.torneoya.viewmodel.partido.*
import mingosgit.josecr.torneoya.viewmodel.usuario.AdministrarPartidosViewModel
import mingosgit.josecr.torneoya.viewmodel.usuario.MisJugadoresViewModel
import mingosgit.josecr.torneoya.viewmodel.usuario.UsuarioLocalViewModel
import mingosgit.josecr.torneoya.viewmodel.usuario.CrearEquipoPredefinidoViewModel
import mingosgit.josecr.torneoya.viewmodel.equipopredefinido.EquiposPredefinidosViewModel

import mingosgit.josecr.torneoya.viewmodel.usuario.LoginViewModel
import mingosgit.josecr.torneoya.viewmodel.usuario.RegisterViewModel
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

    val usuarioId = runBlocking {
        usuarioLocalRepository.getUsuario()?.id ?: 0L
    }

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
            val globalUserViewModel: GlobalUserViewModel = viewModel()
            AmigosScreen(
                navController = navController,
                globalUserViewModel = globalUserViewModel
            )
        }

        composable("solicitudes_pendientes") {
            SolicitudesPendientesScreen(navController = navController)
        }
        composable(BottomNavItem.Usuario.route) {
            UsuarioScreen(usuarioLocalViewModel, navController, globalUserViewModel)
        }
        composable("login") {
            val loginViewModel = viewModel<LoginViewModel>(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return LoginViewModel(UsuarioAuthRepository()) as T
                    }
                }
            )
            LoginScreen(navController, loginViewModel, globalUserViewModel)
        }
        composable("register") {
            val registerViewModel = viewModel<RegisterViewModel>(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return RegisterViewModel(UsuarioAuthRepository()) as T
                    }
                }
            )
            RegisterScreen(navController, registerViewModel)
        }
        composable(
            route = "confirmar_correo/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            ConfirmarCorreoScreen(navController, correoElectronico = email) {
                navController.navigate("usuario") {
                    popUpTo("register") { inclusive = true }
                }
            }
        }
        composable("crear_partido") {
            val createPartidoViewModel = viewModel(
                modelClass = CreatePartidoViewModel::class.java,
                viewModelStoreOwner = owner,
                factory = CreatePartidoViewModelFactory(partidoRepository, equipoRepositoryInst)
            )
            val equiposPredefinidosVM = viewModel<EquiposPredefinidosViewModel>(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return EquiposPredefinidosViewModel(
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
        composable("mis_jugadores") {
            val misJugadoresViewModel = viewModel<MisJugadoresViewModel>(
                modelClass = MisJugadoresViewModel::class.java,
                viewModelStoreOwner = owner,
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return MisJugadoresViewModel(jugadorRepositoryInst) as T
                    }
                }
            )
            MisJugadoresScreen(navController, misJugadoresViewModel)
        }
        composable(
            "estadisticas_jugador/{jugadorId}",
            arguments = listOf(navArgument("jugadorId") { type = NavType.LongType })
        ) { backStackEntry ->
            val jugadorId = backStackEntry.arguments?.getLong("jugadorId") ?: return@composable
            EstadisticasJugadorScreen(
                navController = navController,
                jugadorId = jugadorId
            )
        }
        composable(
            "visualizar_partido/{partidoId}",
            arguments = listOf(navArgument("partidoId") { type = NavType.LongType })
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
            route = "asignar_jugadores/{partidoId}?equipoAId={equipoAId}&equipoBId={equipoBId}&fecha={fecha}&horaInicio={horaInicio}&numeroPartes={numeroPartes}&tiempoPorParte={tiempoPorParte}&tiempoDescanso={tiempoDescanso}&numeroJugadores={numeroJugadores}&equipoAPredefinidoId={equipoAPredefinidoId}&equipoBPredefinidoId={equipoBPredefinidoId}",
            arguments = listOf(
                navArgument("partidoId") { type = NavType.LongType },
                navArgument("equipoAId") { type = NavType.LongType; defaultValue = -1L },
                navArgument("equipoBId") { type = NavType.LongType; defaultValue = -1L },
                navArgument("fecha") { defaultValue = "" },
                navArgument("horaInicio") { defaultValue = "" },
                navArgument("numeroPartes") { defaultValue = "2" },
                navArgument("tiempoPorParte") { defaultValue = "25" },
                navArgument("tiempoDescanso") { defaultValue = "5" },
                navArgument("numeroJugadores") { defaultValue = "5" },
                navArgument("equipoAPredefinidoId") { type = NavType.LongType; defaultValue = -1L },
                navArgument("equipoBPredefinidoId") { type = NavType.LongType; defaultValue = -1L }
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
        composable("partidos_lista_busqueda") {
            val administrarViewModel = viewModel<AdministrarPartidosViewModel>(
                modelClass = AdministrarPartidosViewModel::class.java,
                viewModelStoreOwner = owner,
                factory = AdministrarPartidosViewModel.Factory(
                    partidoRepository,
                    goleadorRepository,
                    eventoRepository
                )
            )
            PartidosListaBusquedaScreen(
                viewModel = administrarViewModel,
                navController = navController
            )
        }
        composable(
            "administrar_partido_goles/{partidoId}",
            arguments = listOf(navArgument("partidoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val partidoId = backStackEntry.arguments?.getLong("partidoId") ?: return@composable
            val administrarViewModel = viewModel<AdministrarPartidosViewModel>(
                modelClass = AdministrarPartidosViewModel::class.java,
                viewModelStoreOwner = owner,
                factory = AdministrarPartidosViewModel.Factory(
                    partidoRepository,
                    goleadorRepository,
                    eventoRepository
                )
            )
            val partidos = administrarViewModel.partidos.collectAsState().value
            val partido = partidos.find { it.id == partidoId }
            if (partido != null) {
                val jugadoresEquipoA = runBlocking {
                    db.partidoEquipoJugadorDao()
                        .getJugadoresDeEquipoEnPartido(partidoId, partido.equipoAId)
                        .mapNotNull { rel ->
                            db.jugadorDao().getById(rel.jugadorId)?.let { jugador ->
                                Pair(jugador.id, jugador.nombre)
                            }
                        }
                }
                val jugadoresEquipoB = runBlocking {
                    db.partidoEquipoJugadorDao()
                        .getJugadoresDeEquipoEnPartido(partidoId, partido.equipoBId)
                        .mapNotNull { rel ->
                            db.jugadorDao().getById(rel.jugadorId)?.let { jugador ->
                                Pair(jugador.id, jugador.nombre)
                            }
                        }
                }
                val nombreEquipoA = runBlocking {
                    db.equipoDao().getById(partido.equipoAId)?.nombre ?: "Equipo A"
                }
                val nombreEquipoB = runBlocking {
                    db.equipoDao().getById(partido.equipoBId)?.nombre ?: "Equipo B"
                }
                AdministrarPartidosScreen(
                    partido = partido,
                    viewModel = administrarViewModel,
                    navController = navController,
                    equipoAId = partido.equipoAId,
                    equipoBId = partido.equipoBId,
                    jugadoresEquipoA = jugadoresEquipoA,
                    jugadoresEquipoB = jugadoresEquipoB,
                    nombreEquipoA = nombreEquipoA,
                    nombreEquipoB = nombreEquipoB
                )
            }
        }
        composable("equipos_predefinidos") {
            val equiposPredefinidosVM = viewModel<EquiposPredefinidosViewModel>(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return EquiposPredefinidosViewModel(
                            equipoPredefinidoRepository
                        ) as T
                    }
                }
            )
            EquiposPredefinidosScreen(
                navController = navController,
                viewModel = equiposPredefinidosVM
            )
        }
        composable("crear_equipo_predefinido") {
            val crearEquipoPredefinidoVM = viewModel<CrearEquipoPredefinidoViewModel>(
                factory = CrearEquipoPredefinidoViewModel.Factory(
                    equipoPredefinidoRepository,
                    jugadorRepositoryInst
                )
            )
            CrearEquipoPredefinidoScreen(
                navController = navController,
                viewModel = crearEquipoPredefinidoVM
            )
        }
    }
}
