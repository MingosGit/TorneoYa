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
            // BORRAR PARTIDOS CREADOS POR EL USUARIO
            val creados = partidoRepo.listarPartidosPorUsuario(uid).filter { it.creadorUid == uid }
            for (p in creados) {
                // Borrar comentarios del partido
                val comentarios = firestore.collection("comentarios")
                    .whereEqualTo("partidoUid", p.uid)
                    .get().await()
                for (doc in comentarios.documents) {
                    firestore.collection("comentarios").document(doc.id).delete().await()
                }

                // Borrar votos a comentarios
                val votosComentarios = firestore.collection("comentario_votos")
                    .whereEqualTo("partidoUid", p.uid)
                    .get().await()
                for (doc in votosComentarios.documents) {
                    firestore.collection("comentario_votos").document(doc.id).delete().await()
                }

                // Borrar encuestas
                val encuestas = firestore.collection("encuestas")
                    .whereEqualTo("partidoUid", p.uid)
                    .get().await()
                for (encuestaDoc in encuestas.documents) {
                    val encuestaId = encuestaDoc.id
                    firestore.collection("encuestas").document(encuestaId).delete().await()

                    // Borrar votos de encuesta
                    val votosEncuesta = firestore.collection("encuesta_votos")
                        .whereEqualTo("encuestaUid", encuestaId)
                        .get().await()
                    for (doc in votosEncuesta.documents) {
                        firestore.collection("encuesta_votos").document(doc.id).delete().await()
                    }
                }

                // Borrar el partido
                partidoRepo.borrarPartido(p.uid)
            }

            // BORRAR COMENTARIOS SUELTOS DEL USUARIO
            val comentariosUsuario = firestore.collection("comentarios")
                .whereEqualTo("usuarioUid", uid)
                .get().await()
            for (doc in comentariosUsuario.documents) {
                firestore.collection("comentarios").document(doc.id).delete().await()
            }

            // BORRAR VOTOS DEL USUARIO
            val votosUsuario = firestore.collection("comentario_votos")
                .whereEqualTo("usuarioUid", uid)
                .get().await()
            for (doc in votosUsuario.documents) {
                firestore.collection("comentario_votos").document(doc.id).delete().await()
            }

            val votosEncuestasUsuario = firestore.collection("encuesta_votos")
                .whereEqualTo("usuarioUid", uid)
                .get().await()
            for (doc in votosEncuestasUsuario.documents) {
                firestore.collection("encuesta_votos").document(doc.id).delete().await()
            }

            // BORRAR USUARIO EN FIRESTORE
            firestore.collection("usuarios").document(uid).delete().await()

            // BORRAR CUENTA EN AUTH
            user.delete().await()
        }
    }
}
