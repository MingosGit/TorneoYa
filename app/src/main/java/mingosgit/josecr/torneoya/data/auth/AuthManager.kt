package mingosgit.josecr.torneoya.data.auth

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mingosgit.josecr.torneoya.data.connectivity.NetworkMonitor
import mingosgit.josecr.torneoya.data.session.SessionSnapshot
import mingosgit.josecr.torneoya.data.session.SessionStore

class AuthManager(
    app: Application
) {
    // Constructor de AuthManager, inicializa dependencias y estado de autenticación
    private val appScope = CoroutineScope(Dispatchers.IO + Job())
    private val sessionStore = SessionStore(app.applicationContext)
    private val network = NetworkMonitor(app.applicationContext)
    private val auth by lazy { FirebaseAuth.getInstance() }

    private val _state = MutableStateFlow<AuthState>(AuthState.SignedOut)
    val state: StateFlow<AuthState> = _state

    private val _online = network.isOnline()
        .stateIn(appScope, SharingStarted.Eagerly, false)

    init {
        // Inicializa el flujo que combina sesión guardada y estado de conexión
        appScope.launch {
            combine(sessionStore.session, _online) { session, online ->
                session to online
            }
                .onEach { (session, online) ->
                    val firebaseUser = auth.currentUser
                    when {
                        firebaseUser == null && !session.hasCachedSession -> {
                            _state.value = AuthState.SignedOut
                        }
                        firebaseUser == null && session.hasCachedSession -> {
                            // Usuario cacheado sin sesión de Firebase (app offline o token perdido)
                            _state.value = AuthState.SignedInCached(session)
                            if (online) validateOnline(session)
                        }
                        firebaseUser != null && !online -> {
                            // Sesión válida pero sin red: mantener cache
                            _state.value = AuthState.SignedInCached(
                                session.copy(uid = firebaseUser.uid)
                            )
                        }
                        firebaseUser != null && online -> {
                            validateOnline(session)
                        }
                    }
                }
                .collect { /* no-op */ }
        }
    }

    // Método que valida la sesión online contra Firebase y actualiza el estado
    private suspend fun validateOnline(current: SessionSnapshot) {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            if (current.hasCachedSession) {
                _state.value = AuthState.SignedInCached(current)
            } else {
                _state.value = AuthState.SignedOut
            }
            return
        }
        try {
            firebaseUser.reload().await()
            val verified = firebaseUser.isEmailVerified
            sessionStore.upsert(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                isEmailVerified = verified,
                lastTokenRefreshAtMillis = System.currentTimeMillis()
            )
            val session = sessionStore.session.stateIn(appScope, SharingStarted.Eagerly, current).value
            _state.value = AuthState.SignedInOnline(session)
        } catch (e: Exception) {
            // No tumbar sesión por error de red; marcar atención solo si es error real de auth con red.
            val message = e.message ?: "Error desconocido"
            // Mantén sesión cacheada y pide atención en UI (sin cerrar).
            val snapshot = sessionStore.session.stateIn(appScope, SharingStarted.Eagerly, current).value
            _state.value = AuthState.SignedInNeedsAttention(snapshot, message)
        }
    }

    // Actualiza en caché el perfil del usuario (nombre y avatar)
    suspend fun updateProfileCache(nombreUsuario: String?, avatar: Int?) {
        sessionStore.upsert(
            nombreUsuario = nombreUsuario,
            avatar = avatar ?: -1
        )
    }

    // Guarda en caché un login con los datos de usuario
    suspend fun cacheLogin(uid: String, email: String, nombreUsuario: String?, avatar: Int?) {
        sessionStore.upsert(
            uid = uid,
            email = email,
            nombreUsuario = nombreUsuario ?: "",
            avatar = avatar ?: -1
        )
    }

    // Cierra sesión localmente y limpia datos de la app
    suspend fun signOutLocalOnly() {
        // Cerrar sesión local y Firebase (cuando haya red). Aquí limpiamos estado de la app.
        try { FirebaseAuth.getInstance().signOut() } catch (_: Exception) {}
        sessionStore.clear()
        _state.value = AuthState.SignedOut
    }

    // Devuelve el flujo de conectividad online
    fun online(): StateFlow<Boolean> = _online
}
