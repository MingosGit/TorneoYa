package mingosgit.josecr.torneoya.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mingosgit.josecr.torneoya.data.firebase.PartidoFirebase
import mingosgit.josecr.torneoya.data.firebase.PartidoFirebaseRepository
import mingosgit.josecr.torneoya.data.firebase.EquipoFirebase
import mingosgit.josecr.torneoya.data.firebase.NotificacionFirebaseRepository
import mingosgit.josecr.torneoya.data.database.AppDatabase

data class HomeUiState(
    val nombreUsuario: String = "",
    val partidosTotales: Int = 0,
    val equiposTotales: Int = 0,
    val jugadoresTotales: Int = 0,
    val amigosTotales: Int = 0
)

data class HomeProximoPartidoUi(
    val partido: PartidoFirebase,
    val nombreEquipoA: String,
    val nombreEquipoB: String
)

class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {

    // Repos locales dentro del VM para evitar factory personalizado
    private val partidoRepo: PartidoFirebaseRepository = PartidoFirebaseRepository()
    private val notificacionRepo: NotificacionFirebaseRepository = NotificacionFirebaseRepository()
    private val appDb by lazy { AppDatabase.getInstance(getApplication()) }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _proximoPartidoUi = MutableStateFlow<HomeProximoPartidoUi?>(null)
    val proximoPartidoUi: StateFlow<HomeProximoPartidoUi?> = _proximoPartidoUi.asStateFlow()

    private val _cargandoProx = MutableStateFlow(true)
    val cargandoProx: StateFlow<Boolean> = _cargandoProx.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    init {
        cargarDatosOnline()
        cargarProximoPartido()
        cargarUnreadCount()
    }

    fun recargarDatos() {
        cargarDatosOnline()
        cargarProximoPartido()
        cargarUnreadCount()
    }

    private fun cargarDatosOnline() {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                _uiState.value = HomeUiState()
                _unreadCount.value = 0
                return@launch
            }
            val uid = user.uid
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()

            // Nombre usuario
            val usuarioSnap = firestore.collection("usuarios").document(uid).get().await()
            val nombreUsuario = usuarioSnap.getString("nombreUsuario") ?: "Usuario"

            // PARTIDOS
            val partidosSnap = firestore.collection("partidos").get().await()
            val partidosPropios = partidosSnap.documents.filter { doc ->
                (doc.getString("creadorUid") == uid)
                        || ((doc.get("usuariosConAcceso") as? List<*>)?.contains(uid) == true)
            }
            val partidosTotales = partidosPropios.size

            // EQUIPOS (ejemplo)
            val equiposSnap = firestore.collection("equipos").get().await()
            val equiposPropios = equiposSnap.documents.filter { doc ->
                (doc.get("miembros") as? List<*>)?.contains(uid) == true
            }
            val equiposTotales = equiposPropios.size

            // JUGADORES (solo los tuyos)
            val jugadoresSnap = firestore.collection("jugadores")
                .whereEqualTo("usuarioUid", uid).get().await()
            val jugadoresTotales = jugadoresSnap.size()

            // AMIGOS
            val amigosSnap = firestore.collection("usuarios").document(uid)
                .collection("amigos").get().await()
            val amigosTotales = amigosSnap.size()

            _uiState.value = HomeUiState(
                nombreUsuario = nombreUsuario,
                partidosTotales = partidosTotales,
                equiposTotales = equiposTotales,
                jugadoresTotales = jugadoresTotales,
                amigosTotales = amigosTotales
            )
        }
    }

    fun setNombreUsuarioDirecto(nombre: String) {
        _uiState.value = _uiState.value.copy(nombreUsuario = nombre)
    }

    private fun cargarProximoPartido() {
        viewModelScope.launch {
            _cargandoProx.value = true
            val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            var partidoUi: HomeProximoPartidoUi? = null
            if (userUid.isNotBlank()) {
                val partidos = partidoRepo.listarPartidos()
                    .filter { it.estado == "PREVIA" }
                    .filter { partido ->
                        partido.creadorUid == userUid ||
                                partido.usuariosConAcceso.contains(userUid) ||
                                partido.administradores.contains(userUid)
                    }
                    .map { partido ->
                        val fechaHora = try {
                            val partes = partido.fecha.split("-") // DD-MM-YYYY
                            val fechaNormalizada = "${partes[2]}-${partes[1]}-${partes[0]}" // YYYY-MM-DD
                            val fechaHoraStr = "$fechaNormalizada ${partido.horaInicio}"
                            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
                            sdf.parse(fechaHoraStr)?.time ?: Long.MAX_VALUE
                        } catch (e: Exception) {
                            Long.MAX_VALUE
                        }
                        Pair(partido, fechaHora)
                    }
                    .filter { it.second >= System.currentTimeMillis() }
                    .sortedBy { it.second }

                val proximoPartido = partidos.firstOrNull()?.first

                if (proximoPartido != null) {
                    val nombreEquipoA = if (proximoPartido.equipoAId.isNotBlank()) {
                        val eq = partidoRepo.obtenerEquipo(proximoPartido.equipoAId)
                        eq?.nombre ?: "Equipo A"
                    } else {
                        "Equipo A"
                    }
                    val nombreEquipoB = if (proximoPartido.equipoBId.isNotBlank()) {
                        val eq = partidoRepo.obtenerEquipo(proximoPartido.equipoBId)
                        eq?.nombre ?: "Equipo B"
                    } else {
                        "Equipo B"
                    }
                    partidoUi = HomeProximoPartidoUi(
                        partido = proximoPartido,
                        nombreEquipoA = nombreEquipoA,
                        nombreEquipoB = nombreEquipoB
                    )
                }
            }
            _proximoPartidoUi.value = partidoUi
            _cargandoProx.value = false
        }
    }

    private fun cargarUnreadCount() {
        viewModelScope.launch {
            val userUid = FirebaseAuth.getInstance().currentUser?.uid
            if (userUid == null) {
                _unreadCount.value = 0
                return@launch
            }
            // Descarga todas las notificaciones (globales + personales)
            val todas = notificacionRepo.obtenerNotificaciones(userUid)

            // Aplica filtros locales con DAO (archivadas y borradas)
            val archivadas = appDb.notificacionArchivadaDao().getArchivadasUids().toSet()
            val borradas = appDb.notificacionBorradaDao().getBorradasUids().toSet()

            val noLeidas = todas.filter { it.uid !in archivadas && it.uid !in borradas }
            _unreadCount.value = noLeidas.size
        }
    }
}
