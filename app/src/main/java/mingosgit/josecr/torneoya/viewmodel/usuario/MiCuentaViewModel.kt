package mingosgit.josecr.torneoya.viewmodel.usuario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mingosgit.josecr.torneoya.data.firebase.PartidoFirebaseRepository
import mingosgit.josecr.torneoya.repository.UsuarioAuthRepository

class MiCuentaViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val partidoRepo = PartidoFirebaseRepository()
    private val usuarioAuthRepo = UsuarioAuthRepository()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _nombreUsuario = MutableStateFlow("")
    val nombreUsuario: StateFlow<String> = _nombreUsuario

    private val _confirmarCerrarSesion = MutableStateFlow(false)
    val confirmarCerrarSesion: StateFlow<Boolean> = _confirmarCerrarSesion

    private val _confirmarEliminarCuenta = MutableStateFlow(false)
    val confirmarEliminarCuenta: StateFlow<Boolean> = _confirmarEliminarCuenta

    private val _errorCambioNombre = MutableStateFlow<String?>(null)
    val errorCambioNombre: StateFlow<String?> = _errorCambioNombre

    fun cargarDatos() {
        val user = auth.currentUser ?: return
        _email.value = user.email ?: ""
        viewModelScope.launch {
            val snap = firestore.collection("usuarios").document(user.uid).get().await()
            _nombreUsuario.value = snap.getString("nombreUsuario") ?: ""
        }
    }

    fun cambiarNombreUsuario(nuevoNombre: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            if (!usuarioAuthRepo.isNombreUsuarioDisponible(nuevoNombre)) {
                _errorCambioNombre.value = "El nombre de usuario ya está en uso"
                return@launch
            }
            firestore.collection("usuarios").document(uid)
                .update("nombreUsuario", nuevoNombre).await()
            _nombreUsuario.value = nuevoNombre
            _errorCambioNombre.value = null
        }
    }

    fun cerrarSesion() {
        auth.signOut()
    }

    fun confirmarCerrarSesionDialog(show: Boolean) {
        _confirmarCerrarSesion.value = show
    }

    fun confirmarEliminarCuentaDialog(show: Boolean) {
        _confirmarEliminarCuenta.value = show
    }

    fun eliminarCuentaYDatos() {
        val user = auth.currentUser ?: return
        val uid = user.uid
        viewModelScope.launch {
            val creados = partidoRepo.listarPartidosPorUsuario(uid).filter { it.creadorUid == uid }
            for (p in creados) {
                partidoRepo.borrarPartido(p.uid)
            }
            firestore.collection("usuarios").document(uid).delete().await()
            user.delete().await()
        }
    }
}
