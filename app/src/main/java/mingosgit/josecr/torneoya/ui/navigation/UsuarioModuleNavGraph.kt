package mingosgit.josecr.torneoya.ui.navigation

import UsuarioScreen
import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import kotlinx.coroutines.runBlocking
import mingosgit.josecr.torneoya.data.database.AppDatabase
import mingosgit.josecr.torneoya.repository.*
import mingosgit.josecr.torneoya.ui.screens.usuario.*
import mingosgit.josecr.torneoya.ui.screens.equipopredefinido.CrearEquipoPredefinidoScreen
import mingosgit.josecr.torneoya.ui.screens.equipopredefinido.EquiposPredefinidosScreen
import mingosgit.josecr.torneoya.viewmodel.usuario.*
import mingosgit.josecr.torneoya.viewmodel.equipopredefinido.EquiposPredefinidosViewModel

import androidx.navigation.NavGraphBuilder

@SuppressLint("StateFlowValueCalledInComposition")
@Suppress("FunctionName")
fun NavGraphBuilder.addUsuarioModuleNavGraph(
    navController: NavHostController,
    owner: ViewModelStoreOwner,
    usuarioLocalViewModel: UsuarioLocalViewModel,
    globalUserViewModel: GlobalUserViewModel,
    db: AppDatabase,
    partidoRepository: PartidoRepository,
    equipoRepositoryInst: EquipoRepository,
    usuarioLocalRepository: UsuarioLocalRepository,
    jugadorRepositoryInst: JugadorRepository,
    eventoRepository: EventoRepository,
    goleadorRepository: GoleadorRepository,
    comentarioRepository: ComentarioRepository,
    encuestaRepository: EncuestaRepository,
    relacionRepositoryInst: PartidoEquipoJugadorRepository,
    equipoPredefinidoRepository: EquipoPredefinidoRepository
) {
    composable(BottomNavItem.Usuario.route) {
        UsuarioScreen(usuarioLocalViewModel, navController, globalUserViewModel)
    }
    composable("login") {
        val loginViewModel = viewModel<LoginViewModel>(
            factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return LoginViewModel(UsuarioAuthRepository()) as T
                }
            }
        )
        LoginScreen(navController, loginViewModel, globalUserViewModel)
    }
    composable("register") {
        val registerViewModel = viewModel<RegisterViewModel>(
            factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
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
    composable("mis_jugadores") {
        val misJugadoresViewModel = viewModel<MisJugadoresViewModel>(
            modelClass = MisJugadoresViewModel::class.java,
            viewModelStoreOwner = owner,
            factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
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
        val partidosList = administrarViewModel.partidos.value
        val partido = partidosList.find { it.id == partidoId }
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
            factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
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
    composable("crop_image_dialog") {
        CropImageDialog(
            navController = navController,
            uri = TODO(),
            onDismiss = TODO(),
            onCropDone = TODO()
        )
    }
}
