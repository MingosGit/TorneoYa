package mingosgit.josecr.torneoya.ui.screens.partido.visualizarpartidoscreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mingosgit.josecr.torneoya.viewmodel.partido.VisualizarPartidoViewModel

@Composable
fun PartidoTabComentarios(vm: VisualizarPartidoViewModel) {
    val state by vm.comentariosEncuestasState.collectAsState()
    var textoComentario by remember { mutableStateOf("") }
    val usuarioNombre = "Tú"
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = textoComentario,
            onValueChange = { textoComentario = it },
            label = { Text("Escribe un comentario") },
            singleLine = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = {
                if (textoComentario.isNotBlank()) {
                    vm.agregarComentario(usuarioNombre, textoComentario)
                    textoComentario = ""
                }
            })
        )
        Button(
            onClick = {
                if (textoComentario.isNotBlank()) {
                    vm.agregarComentario(usuarioNombre, textoComentario)
                    textoComentario = ""
                }
            },
            modifier = Modifier.align(Alignment.End).padding(end = 8.dp)
        ) {
            Text("Enviar")
        }
        LazyColumn(modifier = Modifier.fillMaxHeight()) {
            items(state.comentarios) { comentario ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp, horizontal = 8.dp)
                ) {
                    Text(
                        text = comentario.usuarioNombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = comentario.texto,
                        fontSize = 16.sp
                    )
                    Text(
                        text = comentario.fechaHora,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Divider()
            }
            if (state.comentarios.isEmpty()) {
                item {
                    Text(
                        text = "Sin comentarios",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(vertical = 32.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
