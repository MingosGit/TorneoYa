package mingosgit.josecr.torneoya.ui.screens.usuario

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.viewmodel.usuario.LoginViewModel
import mingosgit.josecr.torneoya.viewmodel.usuario.LoginState
import mingosgit.josecr.torneoya.viewmodel.usuario.GlobalUserViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    loginViewModel: LoginViewModel,
    globalUserViewModel: GlobalUserViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginState by loginViewModel.loginState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Iniciar Sesión", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it.trim() },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { loginViewModel.login(email, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = loginState != LoginState.Loading && email.isNotBlank() && password.length >= 6
        ) {
            Text("Entrar")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                loginViewModel.clearState()
                navController.navigate("register")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("¿No tienes cuenta? Regístrate")
        }

        when (loginState) {
            is LoginState.Error -> {
                Text(
                    text = (loginState as LoginState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            is LoginState.Success -> {
                Text(
                    text = "¡Sesión iniciada correctamente!",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 12.dp)
                )
                val nombreUsuarioOnline = (loginState as LoginState.Success).usuario.nombreUsuario
                LaunchedEffect(Unit) {
                    globalUserViewModel.setNombreUsuarioOnline(nombreUsuarioOnline)
                    navController.navigate("usuario") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
            LoginState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.padding(top = 12.dp))
            }
            else -> Unit
        }
    }
}
