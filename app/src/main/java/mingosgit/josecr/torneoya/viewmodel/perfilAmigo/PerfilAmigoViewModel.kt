package mingosgit.josecr.torneoya.viewmodel.perfilAmigo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class PerfilAmigoUiState(
    val uid: String = "",
    val nombreUsuario: String? = null,
    val avatar: Int? = null,
    val goles: Int? = null,
    val asistencias: Int? = null,
    val partidosJugados: Int? = null,
    val promedioGoles: Double? = null
)

class PerfilAmigoViewModel(private val amigoUid: String) : ViewModel() {
    private val _state = MutableStateFlow(PerfilAmigoUiState(uid = amigoUid))
    val state: StateFlow<PerfilAmigoUiState> = _state

    // init: dispara la carga del perfil nada más crear el ViewModel
    init {
        cargarPerfil()
    }

    // cargarPerfil: lee datos del amigo en Firestore y actualiza el StateFlow
    private fun cargarPerfil() {
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()

            // Lectura del documento de usuario
            val userDoc = db.collection("usuarios").document(amigoUid).get().await()
            val nombre = userDoc.getString("nombreUsuario")
            val avatar = userDoc.getLong("avatar")?.toInt()
            val partidosJugados = userDoc.getLong("partidosJugados")?.toInt() ?: 0

            // Consulta de goles marcados por el amigo
            val golesSnapshot = db.collection("goleadores")
                .whereEqualTo("jugadorUid", amigoUid)
                .get().await()
            val golesCount = golesSnapshot.size()

            // Consulta de asistencias dadas por el amigo
            val asistenciasSnapshot = db.collection("goleadores")
                .whereEqualTo("asistenciaJugadorUid", amigoUid)
                .get().await()
            val asistenciasCount = asistenciasSnapshot.size()

            // Cálculo del promedio de goles por partido
            val promedio = if (partidosJugados > 0) golesCount.toDouble() / partidosJugados else 0.0

            // Emite el nuevo estado con todos los datos calculados
            _state.value = PerfilAmigoUiState(
                uid = amigoUid,
                nombreUsuario = nombre,
                avatar = avatar,
                goles = golesCount,
                asistencias = asistenciasCount,
                partidosJugados = partidosJugados,
                promedioGoles = promedio
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val amigoUid: String) : ViewModelProvider.Factory {
        // create: construye el ViewModel inyectando el uid del amigo
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PerfilAmigoViewModel(amigoUid) as T
        }
    }
}
