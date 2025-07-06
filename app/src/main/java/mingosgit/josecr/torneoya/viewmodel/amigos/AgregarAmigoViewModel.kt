package mingosgit.josecr.torneoya.viewmodel.amigos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mingosgit.josecr.torneoya.data.entities.UsuarioFirebaseEntity

class AgregarAmigoViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    sealed class UiState {
        object Idle : UiState()
        data class Busqueda(val usuario: UsuarioFirebaseEntity) : UiState()
        data class Error(val mensaje: String) : UiState()
        object Exito : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    private val _miUid = MutableStateFlow<String?>(null)
    val miUid: StateFlow<String?> = _miUid

    init {
        _miUid.value = auth.currentUser?.uid
    }

    fun buscarPorUid(uid: String) {
        _uiState.value = UiState.Idle
        if (uid.isBlank()) return
        val miUidActual = auth.currentUser?.uid
        if (uid == miUidActual) {
            _uiState.value = UiState.Error("No puedes agregarte a ti mismo.")
            return
        }
        viewModelScope.launch {
            try {
                val doc = firestore.collection("usuarios").document(uid).get().await()
                if (!doc.exists()) {
                    _uiState.value = UiState.Error("No existe ningún usuario con ese UID.")
                    return@launch
                }
                val usuario = doc.toObject(UsuarioFirebaseEntity::class.java)
                if (usuario == null) {
                    _uiState.value = UiState.Error("Usuario no válido.")
                } else {
                    _uiState.value = UiState.Busqueda(usuario)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error al buscar usuario.")
            }
        }
    }

    fun enviarSolicitud(uidDestino: String) {
        _uiState.value = UiState.Idle
        val miUidActual = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                // Evitar enviar varias veces
                val solicitudDoc = firestore.collection("usuarios")
                    .document(uidDestino)
                    .collection("solicitudes_amistad")
                    .document(miUidActual)
                    .get().await()
                if (solicitudDoc.exists()) {
                    _uiState.value = UiState.Error("Ya has enviado solicitud a este usuario.")
                    return@launch
                }
                // Añade la solicitud a la colección del usuario destino
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
                    _uiState.value = UiState.Error("Error enviando solicitud.")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error enviando solicitud.")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AgregarAmigoViewModel() as T
        }
    }

    fun resetUi() {
        _uiState.value = UiState.Idle
    }
}
