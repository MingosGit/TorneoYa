package mingosgit.josecr.torneoya.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mingosgit.josecr.torneoya.viewmodel.UsuarioLocalViewModel

@Composable
fun UsuarioScreen(
    usuarioLocalViewModel: UsuarioLocalViewModel
) {
    LaunchedEffect(Unit) {
        usuarioLocalViewModel.cargarUsuario()
    }

    val usuario by usuarioLocalViewModel.usuario.collectAsState()

    var textFieldValue by remember { mutableStateOf(TextFieldValue(usuario?.nombre ?: "Usuario1")) }
    val nombreActual = usuario?.nombre ?: "Usuario1"

    // Sincroniza el campo de texto si el nombre cambia desde el ViewModel
    LaunchedEffect(nombreActual) {
        textFieldValue = TextFieldValue(
            text = nombreActual,
            selection = TextRange(nombreActual.length)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bienvenido $nombreActual",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                usuarioLocalViewModel.cambiarNombre(it.text)
            },
            label = { Text("Tu nombre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
