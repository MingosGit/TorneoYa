package mingosgit.josecr.torneoya.viewmodel.amigos

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mingosgit.josecr.torneoya.R
import mingosgit.josecr.torneoya.data.entities.UsuarioFirebaseEntity

// ViewModel para buscar usuarios por UID y enviar solicitudes de amistad
class AgregarAmigoViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    // Estado de UI: inactivo, resultado de búsqueda, error o éxito (solicitud enviada)
    sealed class UiState {
        object Idle : UiState()
        data class Busqueda(val usuario: UsuarioFirebaseEntity) : UiState()
        data class Error(@StringRes val resId: Int) : UiState()
        object Exito : UiState()
    }

    // Flujo del estado de UI
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    // Flujo con mi UID actual
    private val _miUid = MutableStateFlow<String?>(null)
    val miUid: StateFlow<String?> = _miUid

    // init: guarda el uid del usuario autenticado (si lo hay)
    init {
        _miUid.value = auth.currentUser?.uid
    }

    // buscarPorUid: consulta Firestore por UID y actualiza el estado con el usuario o error
    fun buscarPorUid(uid: String) {
        _uiState.value = UiState.Idle
        if (uid.isBlank()) return
        val miUidActual = auth.currentUser?.uid
        if (uid == miUidActual) {
            _uiState.value = UiState.Error(R.string.amigos_err_self_add)
            return
        }
        viewModelScope.launch {
            try {
                val doc = firestore.collection("usuarios").document(uid).get().await()
                if (!doc.exists()) {
                    _uiState.value = UiState.Error(R.string.amigos_err_user_not_found)
                    return@launch
                }
                val usuario = doc.toObject(UsuarioFirebaseEntity::class.java)
                if (usuario == null) {
                    _uiState.value = UiState.Error(R.string.amigos_err_invalid_user)
                } else {
                    _uiState.value = UiState.Busqueda(usuario)
                }
            } catch (_: Exception) {
                _uiState.value = UiState.Error(R.string.amigos_err_search)
            }
        }
    }

    // enviarSolicitud: crea la solicitud de amistad en el usuario destino si no existe ya
    fun enviarSolicitud(uidDestino: String) {
        _uiState.value = UiState.Idle
        val miUidActual = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                // Evita duplicados comprobando si ya hay solicitud
                val solicitudDoc = firestore.collection("usuarios")
                    .document(uidDestino)
                    .collection("solicitudes_amistad")
                    .document(miUidActual)
                    .get().await()
                if (solicitudDoc.exists()) {
                    _uiState.value = UiState.Error(R.string.amigos_err_already_sent)
                    return@launch
                }
                // Carga mis datos y los guarda como documento de solicitud en el destino
                val miUsuarioSnap = firestore.collection("usuarios").document(miUidActual).get().await()
                val miUsuario = miUsuarioSnap.toObject(UsuarioFirebaseEntity::class.java)
                if (miUsuario != null) {
                    firestore.collection("usuarios")
                        .document(uidDestino)
                        .collection("solicitudes_amistad")
                        .document(miUidActual)
                        .set(miUsuario)
                        .await()
                    _uiState.value = UiState.Exito
                } else {
                    _uiState.value = UiState.Error(R.string.amigos_err_send)
                }
            } catch (_: Exception) {
                _uiState.value = UiState.Error(R.string.amigos_err_send)
            }
        }
    }

    // Factory: crea instancias del ViewModel para inyección simple
    @Suppress("UNCHECKED_CAST")
    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AgregarAmigoViewModel() as T
        }
    }

    // resetUi: vuelve el estado de UI a Idle
    fun resetUi() {
        _uiState.value = UiState.Idle
    }
}
