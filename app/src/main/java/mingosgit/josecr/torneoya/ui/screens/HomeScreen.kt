package mingosgit.josecr.torneoya.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mingosgit.josecr.torneoya.data.entities.TorneoEntity
import mingosgit.josecr.torneoya.viewmodel.HomeViewModel
import mingosgit.josecr.torneoya.viewmodel.AppViewModelFactory
import mingosgit.josecr.torneoya.viewmodel.PartidoUI
import mingosgit.josecr.torneoya.utils.formatAsDateString

@Composable
fun HomeScreen(
    onCrearPartido: () -> Unit,
    onTorneoClick: (Long) -> Unit,
    onPartidoClick: (Long) -> Unit
) {
    val context = LocalContext.current.applicationContext
    val factory = remember { AppViewModelFactory(context) }
    val viewModel: HomeViewModel = viewModel(
        modelClass = HomeViewModel::class.java,
        factory = factory
    )

    val torneos by viewModel.torneos.collectAsState()
    val partidosUI by viewModel.partidosUI.collectAsState()

    LaunchedEffect(Unit) { viewModel.cargarDatos() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onCrearPartido) {
                Text("+") // O usa un Icon
            }
        }
    ) { innerPadding ->
        if (torneos.isEmpty() && partidosUI.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("¡No hay torneos ni partidos!", style = MaterialTheme.typography.titleMedium)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                if (torneos.isNotEmpty()) {
                    item {
                        Text("Torneos activos", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                    }
                    items(torneos) { torneo ->
                        TorneoCard(torneo, onTorneoClick)
                        Spacer(Modifier.height(12.dp))
                    }
                }
                if (partidosUI.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(24.dp))
                        Text("Próximos partidos", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                    }
                    items(partidosUI) { partido ->
                        PartidoCard(partido, { onPartidoClick(partido.id) })
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}


@Composable
fun PartidoCard(partido: PartidoUI, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("${partido.nombreEquipoLocal} vs ${partido.nombreEquipoVisitante}", style = MaterialTheme.typography.titleMedium)
                Text("Fecha: ${partido.fecha.formatAsDateString()}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
@Composable
fun TorneoCard(torneo: TorneoEntity, onClick: (Long) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(torneo.id) },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(torneo.nombre, style = MaterialTheme.typography.titleMedium)
            Text(
                if (torneo.formato == "liga") "Liga" else "Eliminatoria",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

