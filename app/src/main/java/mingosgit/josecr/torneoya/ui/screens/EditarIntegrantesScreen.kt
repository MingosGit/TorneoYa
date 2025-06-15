package mingosgit.josecr.torneoya.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mingosgit.josecr.torneoya.viewmodel.AppViewModelFactory
import mingosgit.josecr.torneoya.viewmodel.PartidoViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

@Composable
fun EditarIntegrantesScreen(
    partidoId: Long,
    navController: NavController
) {
    val context = LocalContext.current.applicationContext
    val factory = remember { AppViewModelFactory(context) }
    val viewModel: PartidoViewModel = viewModel(
        modelClass = PartidoViewModel::class.java,
        factory = factory
    )
    val ui by viewModel.ui.collectAsState()

    val local = remember { mutableStateListOf<String>() }
    val visitante = remember { mutableStateListOf<String>() }
    var initialized by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    LaunchedEffect(partidoId) {
        viewModel.cargarPartidoCompleto(partidoId)
    }

    if (ui == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (!initialized) {
        local.clear(); local.addAll(ui!!.integrantesLocal)
        visitante.clear(); visitante.addAll(ui!!.integrantesVisitante)
        initialized = true
    }

    val hayCamposVacios = local.any { it.trim().isBlank() } || visitante.any { it.trim().isBlank() }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Editar nombres de los integrantes", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        Text("Equipo local: ${ui!!.nombreEquipoLocal}", style = MaterialTheme.typography.titleMedium)
        local.forEachIndexed { idx, nombre ->
            OutlinedTextField(
                value = nombre,
                onValueChange = { local[idx] = it },
                label = { Text("Integrante ${idx + 1}") },
                singleLine = true,
                isError = nombre.trim().isBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            )
        }
        Spacer(Modifier.height(24.dp))

        Text("Equipo visitante: ${ui!!.nombreEquipoVisitante}", style = MaterialTheme.typography.titleMedium)
        visitante.forEachIndexed { idx, nombre ->
            OutlinedTextField(
                value = nombre,
                onValueChange = { visitante[idx] = it },
                label = { Text("Integrante ${idx + 1}") },
                singleLine = true,
                isError = nombre.trim().isBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            )
        }

        if (showError && hayCamposVacios) {
            Spacer(Modifier.height(16.dp))
            Text("Ningún nombre puede quedar vacío.", color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(32.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (hayCamposVacios) {
                    showError = true
                } else {
                    viewModel.actualizarNombresIntegrantes(local, visitante) {
                        navController.popBackStack()
                    }
                }
            },
            enabled = !hayCamposVacios
        ) {
            Text("Guardar cambios")
        }
    }
}
