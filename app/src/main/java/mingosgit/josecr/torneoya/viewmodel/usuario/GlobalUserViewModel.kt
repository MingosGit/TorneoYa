package mingosgit.josecr.torneoya.viewmodel.usuario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GlobalUserViewModel : ViewModel() {

    private val _nombreUsuarioOnline = MutableStateFlow<String?>(null)
    val nombreUsuarioOnline: StateFlow<String?> = _nombreUsuarioOnline

    fun setNombreUsuarioOnline(nombre: String) {
        _nombreUsuarioOnline.value = nombre
    }

    fun cargarNombreUsuarioOnlineSiSesionActiva() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            viewModelScope.launch {
                val db = FirebaseFirestore.getInstance()
                val usuarioSnap = db.collection("usuarios").document(user.uid).get().await()
                val nombreUsuario = usuarioSnap.getString("nombreUsuario")
                _nombreUsuarioOnline.value = nombreUsuario
            }
        } else {
            _nombreUsuarioOnline.value = null
        }
    }
}
