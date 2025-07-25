package mingosgit.josecr.torneoya.ui.screens.ajustes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen() {
    val opciones = listOf(
        "Mi cuenta",
        "Mi cuenta local",
        "Idioma",
        "Notificaciones",
        "Tema de la app",
        "Datos y privacidad",
        "Ayuda",
        "Créditos",
        "Sobre la aplicación"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(opciones) { opcion ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* Acción futura */ },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(text = opcion, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}
