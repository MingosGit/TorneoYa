package mingosgit.josecr.torneoya.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mingosgit.josecr.torneoya.data.firebase.EquipoFirebase
import mingosgit.josecr.torneoya.data.firebase.PartidoFirebase
import mingosgit.josecr.torneoya.data.firebase.PartidoFirebaseRepository

data class HomeUiState(
    val nombreUsuario: String = "",
    val partidosTotales: Int = 0,
    val equiposTotales: Int = 0,
    val jugadoresTotales: Int = 0,
    val amigosTotales: Int = 0
)


class HomeViewModel(
    private val partidoRepo: PartidoFirebaseRepository = PartidoFirebaseRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _proximoPartidoUi = MutableStateFlow<HomeProximoPartidoUi?>(null)
    val proximoPartidoUi: StateFlow<HomeProximoPartidoUi?> = _proximoPartidoUi.asStateFlow()

    private val _cargandoProx = MutableStateFlow(true)
    val cargandoProx: StateFlow<Boolean> = _cargandoProx.asStateFlow()

    init {
        cargarDatosOnline()
        cargarProximoPartido()
    }

    fun recargarDatos() {
        cargarDatosOnline()
        cargarProximoPartido()
    }

    private fun cargarDatosOnline() {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                _uiState.value = HomeUiState()
                return@launch
            }
            val uid = user.uid
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()

            // Nombre usuario
            val usuarioSnap = firestore.collection("usuarios").document(uid).get().await()
            val nombreUsuario = usuarioSnap.getString("nombreUsuario") ?: "Usuario"

            // PARTIDOS: solo donde es creador o tiene acceso
            val partidosSnap = firestore.collection("partidos").get().await()
            val partidosPropios = partidosSnap.documents.filter { doc ->
                (doc.getString("creadorUid") == uid)
                        || ((doc.get("usuariosConAcceso") as? List<*>)?.contains(uid) == true)
            }
            val partidosTotales = partidosPropios.size

            // EQUIPOS (ejemplo: solo equipos donde sales en "miembros", ajusta esto a tu modelo)
            val equiposSnap = firestore.collection("equipos").get().await()
            val equiposPropios = equiposSnap.documents.filter { doc ->
                (doc.get("miembros") as? List<*>)?.contains(uid) == true
            }
            val equiposTotales = equiposPropios.size

            // JUGADORES (solo los tuyos)
            val jugadoresSnap = firestore.collection("jugadores")
                .whereEqualTo("usuarioUid", uid).get().await()
            val jugadoresTotales = jugadoresSnap.size()

            // AMIGOS (los que tienes en tu subcolección amigos)
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

    private fun cargarProximoPartido() {
        viewModelScope.launch {
            _cargandoProx.value = true
            val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            var partidoUi: HomeProximoPartidoUi? = null
            if (userUid.isNotBlank()) {
                val partidos = partidoRepo
                    .listarPartidosPorUsuario(userUid)
                    .filter { it.estado == "PREVIA" }
                    .sortedBy { it.fecha + " " + it.horaInicio }
                val proximoPartido = partidos.firstOrNull()
                if (proximoPartido != null) {
                    // Conseguir nombres de los equipos (IDs pueden estar vacíos)
                    val nombreEquipoA = when {
                        proximoPartido.nombresManualEquipoA.any { it.isNotBlank() } ->
                            proximoPartido.nombresManualEquipoA.filter { it.isNotBlank() }.joinToString(" / ")
                        proximoPartido.equipoAId.isNotBlank() -> {
                            val eq = partidoRepo.obtenerEquipo(proximoPartido.equipoAId)
                            eq?.nombre ?: "Equipo A"
                        }
                        else -> "Equipo A"
                    }
                    val nombreEquipoB = when {
                        proximoPartido.nombresManualEquipoB.any { it.isNotBlank() } ->
                            proximoPartido.nombresManualEquipoB.filter { it.isNotBlank() }.joinToString(" / ")
                        proximoPartido.equipoBId.isNotBlank() -> {
                            val eq = partidoRepo.obtenerEquipo(proximoPartido.equipoBId)
                            eq?.nombre ?: "Equipo B"
                        }
                        else -> "Equipo B"
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
}
