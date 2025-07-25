package mingosgit.josecr.torneoya.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class HomeUiState(
    val nombreUsuario: String = "",
    val partidosTotales: Int = 0,
    val equiposTotales: Int = 0,
    val jugadoresTotales: Int = 0,
    val amigosTotales: Int = 0
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        cargarDatosOnline()
    }

    fun recargarDatos() {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                _uiState.value = HomeUiState()
                return@launch
            }
            val uid = user.uid
            val firestore = FirebaseFirestore.getInstance()

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
    private fun cargarDatosOnline() {
        viewModelScope.launch {
            val auth = FirebaseAuth.getInstance()
            val firestore = FirebaseFirestore.getInstance()
            val uid = auth.currentUser?.uid

            if (uid == null) {
                _uiState.value = HomeUiState(nombreUsuario = "Usuario")
                return@launch
            }

            // Nombre de usuario
            val usuarioSnap = firestore.collection("usuarios").document(uid).get().await()
            val nombreUsuario = usuarioSnap.getString("nombreUsuario") ?: "Usuario"

            // Partidos (donde tiene acceso o creador)
            val partidosSnap = firestore.collection("partidos")
                .whereArrayContains("usuariosConAcceso", uid)
                .get().await()
            val partidosCreadosSnap = firestore.collection("partidos")
                .whereEqualTo("creadorUid", uid)
                .get().await()
            val partidosTotales = (partidosSnap.size() + partidosCreadosSnap.size())

            // Equipos (puedes personalizar según la app)
            val equiposSnap = firestore.collection("equipos")
                .get().await()
            val equiposTotales = equiposSnap.size()

            // Jugadores (puedes personalizar según la app)
            val jugadoresSnap = firestore.collection("jugadores")
                .get().await()
            val jugadoresTotales = jugadoresSnap.size()

            // Amigos
            val amigosSnap = firestore.collection("usuarios")
                .document(uid)
                .collection("amigos")
                .get().await()
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
}
