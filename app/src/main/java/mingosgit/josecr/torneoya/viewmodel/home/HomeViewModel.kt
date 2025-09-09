package mingosgit.josecr.torneoya.viewmodel.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mingosgit.josecr.torneoya.data.database.AppDatabase
import mingosgit.josecr.torneoya.data.firebase.NotificacionFirebaseRepository
import mingosgit.josecr.torneoya.data.firebase.PartidoFirebase
import mingosgit.josecr.torneoya.data.firebase.PartidoFirebaseRepository
import mingosgit.josecr.torneoya.ui.screens.home.HomeCacheStore
import mingosgit.josecr.torneoya.ui.screens.home.HomeCachedStats
import mingosgit.josecr.torneoya.data.session.SessionStore
import java.text.SimpleDateFormat

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

    private val partidoRepo: PartidoFirebaseRepository = PartidoFirebaseRepository()
    private val notificacionRepo: NotificacionFirebaseRepository = NotificacionFirebaseRepository()
    private val appDb by lazy { AppDatabase.getInstance(getApplication()) }

    private val sessionStore = SessionStore(getApplication())
    private val homeCacheStore = HomeCacheStore(getApplication())

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _proximoPartidoUi = MutableStateFlow<HomeProximoPartidoUi?>(null)
    val proximoPartidoUi: StateFlow<HomeProximoPartidoUi?> = _proximoPartidoUi.asStateFlow()

    private val _cargandoProx = MutableStateFlow(true)
    val cargandoProx: StateFlow<Boolean> = _cargandoProx.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    init {
        // Carga inicial desde caché
        viewModelScope.launch {
            sessionStore.session.collect { s ->
                _uiState.value = _uiState.value.copy(nombreUsuario = s.nombreUsuario.ifBlank { "" })
            }
        }
        viewModelScope.launch {
            homeCacheStore.stats.collect { stats ->
                _uiState.value = _uiState.value.copy(
                    partidosTotales = stats.partidosTotales,
                    equiposTotales = stats.equiposTotales,
                    jugadoresTotales = stats.jugadoresTotales,
                    amigosTotales = stats.amigosTotales
                )
            }
        }

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
                return@launch
            }
            val uid = user.uid
            val firestore = FirebaseFirestore.getInstance()
            try {
                val usuarioSnap = firestore.collection("usuarios").document(uid).get().await()
                val nombreUsuario = usuarioSnap.getString("nombreUsuario") ?: _uiState.value.nombreUsuario

                SessionStore(getApplication()).upsert(
                    uid = uid,
                    email = user.email ?: "",
                    nombreUsuario = nombreUsuario,
                    avatar = usuarioSnap.getLong("avatar")?.toInt() ?: -1
                )

                val partidosSnap = firestore.collection("partidos").get().await()
                val partidosPropios = partidosSnap.documents.filter { doc ->
                    (doc.getString("creadorUid") == uid)
                            || ((doc.get("usuariosConAcceso") as? List<*>)?.contains(uid) == true)
                }
                val partidosTotales = partidosPropios.size

                val equiposSnap = firestore.collection("equipos").get().await()
                val equiposPropios = equiposSnap.documents.filter { doc ->
                    (doc.get("miembros") as? List<*>)?.contains(uid) == true
                }
                val equiposTotales = equiposPropios.size

                val jugadoresSnap = firestore.collection("jugadores")
                    .whereEqualTo("usuarioUid", uid).get().await()
                val jugadoresTotales = jugadoresSnap.size()

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
                homeCacheStore.save(
                    HomeCachedStats(
                        partidosTotales = partidosTotales,
                        equiposTotales = equiposTotales,
                        jugadoresTotales = jugadoresTotales,
                        amigosTotales = amigosTotales
                    )
                )
            } catch (_: Exception) {
                // Offline: mantener caché
            }
        }
    }

    fun setNombreUsuarioDirecto(nombre: String) {
        _uiState.value = _uiState.value.copy(nombreUsuario = nombre)
        viewModelScope.launch { sessionStore.upsert(nombreUsuario = nombre) }
    }

    private fun cargarProximoPartido() {
        viewModelScope.launch {
            _cargandoProx.value = true
            val userUid = FirebaseAuth.getInstance().currentUser?.uid
                ?: sessionStore.session.first().uid
            var partidoUi: HomeProximoPartidoUi? = null
            try {
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
                                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
                                sdf.parse(fechaHoraStr)?.time ?: Long.MAX_VALUE
                            } catch (_: Exception) {
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
            } catch (_: Exception) {
                // Offline: no romper
            }
            _proximoPartidoUi.value = partidoUi
            _cargandoProx.value = false
        }
    }

    private fun cargarUnreadCount() {
        viewModelScope.launch {
            val userUid = FirebaseAuth.getInstance().currentUser?.uid
            if (userUid == null) {
                return@launch
            }
            try {
                val todas = notificacionRepo.obtenerNotificaciones(userUid)
                val archivadas = appDb.notificacionArchivadaDao().getArchivadasUids().toSet()
                val borradas = appDb.notificacionBorradaDao().getBorradasUids().toSet()
                val noLeidas = todas.filter { it.uid !in archivadas && it.uid !in borradas }
                _unreadCount.value = noLeidas.size
            } catch (_: Exception) {
                // Mantener valor previo
            }
        }
    }
}
