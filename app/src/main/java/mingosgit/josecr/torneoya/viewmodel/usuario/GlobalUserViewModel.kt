package mingosgit.josecr.torneoya.viewmodel.usuario

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GlobalUserViewModel(app: Application) : AndroidViewModel(app) {

    private val _nombreUsuarioOnline = MutableStateFlow<String?>(null)
    val nombreUsuarioOnline: StateFlow<String?> = _nombreUsuarioOnline

    private val _sesionOnlineActiva = MutableStateFlow(false)
    val sesionOnlineActiva: StateFlow<Boolean> = _sesionOnlineActiva

    private val _goles = MutableStateFlow<Int?>(null)
    val goles: StateFlow<Int?> = _goles

    private val _asistencias = MutableStateFlow<Int?>(null)
    val asistencias: StateFlow<Int?> = _asistencias

    private val _partidosJugados = MutableStateFlow<Int?>(null)
    val partidosJugados: StateFlow<Int?> = _partidosJugados

    private val _promedioGoles = MutableStateFlow<Double?>(null)
    val promedioGoles: StateFlow<Double?> = _promedioGoles

    private val _avatar = MutableStateFlow<Int?>(null)
    val avatar: StateFlow<Int?> = _avatar

    fun setNombreUsuarioOnline(nombre: String) {
        _nombreUsuarioOnline.value = nombre
    }

    fun cargarNombreUsuarioOnlineSiSesionActiva() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null && user.isEmailVerified) {
            _sesionOnlineActiva.value = true
            viewModelScope.launch {
                val db = FirebaseFirestore.getInstance()
                val usuarioSnap = db.collection("usuarios").document(user.uid).get().await()
                val nombreUsuario = usuarioSnap.getString("nombreUsuario")
                _nombreUsuarioOnline.value = nombreUsuario
                _avatar.value = usuarioSnap.getLong("avatar")?.toInt()
                cargarEstadisticasUsuario(user.uid)
            }
        } else {
            _nombreUsuarioOnline.value = null
            _sesionOnlineActiva.value = false
            _goles.value = null
            _asistencias.value = null
            _partidosJugados.value = null
            _promedioGoles.value = null
            _avatar.value = null
        }
    }

    fun cargarEstadisticasUsuario(uid: String) {
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            // Goles
            val golesSnapshot = db.collection("goleadores")
                .whereEqualTo("jugadorUid", uid)
                .get().await()
            val golesCount = golesSnapshot.size()
            // Asistencias
            val asistenciasSnapshot = db.collection("goleadores")
                .whereEqualTo("asistenciaJugadorUid", uid)
                .get().await()
            val asistenciasCount = asistenciasSnapshot.size()
            // Partidos jugados: leer el contador del usuario
            val usuarioSnap = db.collection("usuarios").document(uid).get().await()
            val partidosJugados = usuarioSnap.getLong("partidosJugados")?.toInt() ?: 0
            val promedio = if (partidosJugados > 0) golesCount.toDouble() / partidosJugados.toDouble() else 0.0

            _goles.value = golesCount
            _asistencias.value = asistenciasCount
            _partidosJugados.value = partidosJugados
            _promedioGoles.value = promedio
        }
    }

    fun cambiarAvatarEnFirebase(avatar: Int) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            try {
                db.collection("usuarios").document(uid)
                    .update("avatar", avatar)
                    .await()
                _avatar.value = avatar
            } catch (e: Exception) {
                // Maneja el error si lo necesitas
            }
        }
    }

    fun cerrarSesionOnline() {
        viewModelScope.launch {
            FirebaseAuth.getInstance().signOut()
            _nombreUsuarioOnline.value = null
            _sesionOnlineActiva.value = false
            _goles.value = null
            _asistencias.value = null
            _partidosJugados.value = null
            _promedioGoles.value = null
            _avatar.value = null
            delay(200)
            reiniciarApp()
        }
    }

    internal fun reiniciarApp() {
        val context = getApplication<Application>().applicationContext
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        Runtime.getRuntime().exit(0)
    }
}
