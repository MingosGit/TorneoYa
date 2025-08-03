package mingosgit.josecr.torneoya.ui.screens.usuario

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.viewmodel.usuario.LoginViewModel
import mingosgit.josecr.torneoya.viewmodel.usuario.LoginState
import mingosgit.josecr.torneoya.viewmodel.usuario.ResetPasswordState
import mingosgit.josecr.torneoya.viewmodel.usuario.GlobalUserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    loginViewModel: LoginViewModel,
    globalUserViewModel: GlobalUserViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginState by loginViewModel.loginState.collectAsState()
    val resetPasswordState by loginViewModel.resetPasswordState.collectAsState()

    val blue = Color(0xFF296DFF)
    val purple = Color(0xFF8F5CFF)
    val backgroundBrush = Brush.verticalGradient(
        0.0f to Color(0xFF181B26),
        0.25f to Color(0xFF22263B),
        0.7f to Color(0xFF1A1E29),
        1.0f to Color(0xFF161622)
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .wrapContentHeight(align = Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Surface(
                shape = CircleShape,
                color = blue.copy(alpha = 0.1f),
                shadowElevation = 0.dp,
                modifier = Modifier
                    .size(76.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Login",
                    tint = blue,
                    modifier = Modifier
                        .padding(17.dp)
                        .fillMaxSize()
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Iniciar Sesión",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Accede a tu cuenta para continuar",
                color = Color(0xFFB7B7D1),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(30.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it.trim() },
                label = { Text("Email") },
                singleLine = true,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.MailOutline, contentDescription = "Email", tint = blue)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF222742), RoundedCornerShape(13.dp))
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Contraseña", tint = purple)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF222742), RoundedCornerShape(13.dp)),
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = { loginViewModel.login(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = loginState != LoginState.Loading && email.isNotBlank() && password.length >= 6,
                colors = ButtonDefaults.buttonColors(containerColor = blue),
                shape = RoundedCornerShape(15.dp)
            ) {
                Text("Entrar", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    loginViewModel.clearState()
                    navController.navigate("register")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(15.dp)
            ) {
                Text("¿No tienes cuenta? Regístrate", color = blue, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            TextButton(
                onClick = { loginViewModel.enviarCorreoRestablecerPassword(email) },
                enabled = email.isNotBlank() && resetPasswordState != ResetPasswordState.Loading,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    "¿Olvidaste tu contraseña?",
                    color = purple,
                    fontWeight = FontWeight.Medium
                )
            }

            AnimatedVisibility(
                visible = loginState is LoginState.Error || loginState is LoginState.Success || loginState is LoginState.Loading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
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
                            color = blue,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                        val nombreUsuarioOnline = (loginState as LoginState.Success).usuario.nombreUsuario
                        LaunchedEffect(Unit) {
                            globalUserViewModel.setNombreUsuarioOnline(nombreUsuarioOnline)
                            // Reiniciar la app al iniciar sesión
                            globalUserViewModel.reiniciarApp()
                        }
                    }
                    LoginState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.padding(top = 12.dp), color = blue)
                    }
                    else -> Unit
                }
            }

            AnimatedVisibility(
                visible = resetPasswordState is ResetPasswordState.Success || resetPasswordState is ResetPasswordState.Error || resetPasswordState is ResetPasswordState.Loading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                when (resetPasswordState) {
                    is ResetPasswordState.Success -> {
                        Text(
                            text = "Correo de recuperación enviado. Revisa tu email.",
                            color = purple,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    is ResetPasswordState.Error -> {
                        Text(
                            text = (resetPasswordState as ResetPasswordState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    ResetPasswordState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp), color = purple)
                    }
                    else -> Unit
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
