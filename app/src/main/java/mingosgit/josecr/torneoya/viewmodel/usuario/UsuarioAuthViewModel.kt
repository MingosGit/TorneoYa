package mingosgit.josecr.torneoya.viewmodel.usuario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Estados de autenticación para la UI
sealed class AuthState {
    object Idle : AuthState()            // Sin acción en curso
    object Loading : AuthState()         // Operación de auth en progreso
    data class Success(val userEmail: String) : AuthState() // Login/registro correcto
    data class Error(val message: String) : AuthState()     // Fallo con mensaje
}

// ViewModel de autenticación: login, registro, logout y gestión de estado
class UsuarioAuthViewModel : ViewModel() {

    // Estado observable de auth
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // Instancia de FirebaseAuth
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Inicia sesión con email y password, actualiza el estado según resultado
    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email.trim(), password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val emailOk = auth.currentUser?.email.orEmpty()
                        _authState.value = AuthState.Success(emailOk)
                    } else {
                        _authState.value = AuthState.Error(task.exception?.localizedMessage ?: "Error desconocido")
                    }
                }
        }
    }

    // Crea cuenta con email y password, y deja al usuario autenticado
    fun register(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email.trim(), password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val emailOk = auth.currentUser?.email.orEmpty()
                        _authState.value = AuthState.Success(emailOk)
                    } else {
                        _authState.value = AuthState.Error(task.exception?.localizedMessage ?: "Error desconocido")
                    }
                }
        }
    }

    // Cierra sesión y limpia el estado a Idle
    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    // Resetea el estado manualmente a Idle (para limpiar mensajes en UI)
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
