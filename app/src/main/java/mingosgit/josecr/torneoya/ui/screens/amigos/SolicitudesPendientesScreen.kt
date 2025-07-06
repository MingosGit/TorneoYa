package mingosgit.josecr.torneoya.ui.screens.amigos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.data.entities.UsuarioFirebaseEntity
import mingosgit.josecr.torneoya.viewmodel.amigos.AmigosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudesPendientesScreen(
    navController: NavController,
    amigosViewModel: AmigosViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = AmigosViewModel.Factory()
    )
) {
    val solicitudes by amigosViewModel.solicitudes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solicitudes de amistad") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (solicitudes.isEmpty()) {
                Text("No tienes solicitudes pendientes", style = MaterialTheme.typography.bodyLarge)
            } else {
                LazyColumn(Modifier.fillMaxWidth()) {
                    items(solicitudes) { solicitud ->
                        SolicitudItem(
                            usuario = solicitud,
                            onAceptar = { amigosViewModel.aceptarSolicitud(solicitud) },
                            onRechazar = { amigosViewModel.rechazarSolicitud(solicitud.uid) }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun SolicitudItem(
    usuario: UsuarioFirebaseEntity,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(usuario.nombreUsuario, style = MaterialTheme.typography.bodyLarge)
            Text(usuario.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Button(
            onClick = onAceptar,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27ae60)),
            modifier = Modifier.padding(end = 4.dp)
        ) { Text("Aceptar") }
        Button(
            onClick = onRechazar,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFc0392b))
        ) { Text("Rechazar") }
    }
}
