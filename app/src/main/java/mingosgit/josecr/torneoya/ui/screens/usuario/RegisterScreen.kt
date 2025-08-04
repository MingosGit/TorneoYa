package mingosgit.josecr.torneoya.ui.screens.usuario

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.viewmodel.usuario.RegisterViewModel
import mingosgit.josecr.torneoya.viewmodel.usuario.RegisterState
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    registerViewModel: RegisterViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nombreUsuario by remember { mutableStateOf("") }
    val registerState by registerViewModel.registerState.collectAsState()

    var navegarAConfirmarCorreo by remember { mutableStateOf(false) }

    val blue = TorneoYaPalette.blue
    val purple = TorneoYaPalette.violet
    val backgroundBrush = Brush.verticalGradient(
        0.0f to Color(0xFF181B26),
        0.25f to Color(0xFF22263B),
        0.7f to Color(0xFF1A1E29),
        1.0f to Color(0xFF161622)
    )

    if (navegarAConfirmarCorreo) {
        ConfirmarCorreoScreen(
            navController = navController,
            correoElectronico = email,
            onVerificado = {
                registerViewModel.clearState()
                navController.navigate("usuario") {
                    popUpTo("register") { inclusive = true }
                }
            }
        )
        return
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp)
                .wrapContentHeight(align = Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))
            Surface(
                shape = CircleShape,
                color = purple.copy(alpha = 0.10f),
                shadowElevation = 0.dp,
                modifier = Modifier.size(74.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Registrar",
                    tint = purple,
                    modifier = Modifier
                        .padding(18.dp)
                        .fillMaxSize()
                )
            }

            Spacer(Modifier.height(15.dp))

            Text(
                text = "Crear Cuenta",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Únete a la comunidad de TorneoYa",
                color = Color(0xFFB7B7D1),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(28.dp))

            OutlinedTextField(
                value = nombreUsuario,
                onValueChange = { nombreUsuario = it.trim() },
                label = { Text("Nombre de usuario único", color = purple) },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = purple)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFF23273D), Color(0xFF1C1D25))),
                        RoundedCornerShape(17.dp)
                    ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.Transparent,
                    focusedBorderColor = purple,
                    unfocusedBorderColor = purple.copy(alpha = 0.6f),
                    cursorColor = purple,
 )
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it.trim() },
                label = { Text("Email", color = blue) },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.MailOutline, contentDescription = null, tint = blue)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFF23273D), Color(0xFF1C1D25))),
                        RoundedCornerShape(17.dp)
                    ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.Transparent,
                    focusedBorderColor = blue,
                    unfocusedBorderColor = blue.copy(alpha = 0.6f),
                    cursorColor = blue,
                )
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña", color = blue) },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = blue)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFF23273D), Color(0xFF1C1D25))),
                        RoundedCornerShape(17.dp)
                    ),
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.Transparent,
                    focusedBorderColor = blue,
                    unfocusedBorderColor = blue.copy(alpha = 0.6f),
                    cursorColor = blue,
                )
            )
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(listOf(purple, blue)),
                        shape = RoundedCornerShape(15.dp)
                    )
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFF23273D), Color(0xFF1C1D25)))
                    )
                    .clickable(enabled = registerState != RegisterState.Loading && email.isNotBlank() && password.length >= 6 && nombreUsuario.isNotBlank()) {
                        registerViewModel.register(email, password, nombreUsuario)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Registrar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = if (registerState != RegisterState.Loading && email.isNotBlank() && password.length >= 6 && nombreUsuario.isNotBlank()) Color.White else Color.White.copy(alpha = 0.4f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(listOf(blue, purple)),
                        shape = RoundedCornerShape(15.dp)
                    )
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFF23273D), Color(0xFF1C1D25)))
                    )
                    .clickable {
                        registerViewModel.clearState()
                        navController.popBackStack()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "¿Ya tienes cuenta? Inicia sesión",
                    color = blue,
                    fontWeight = FontWeight.SemiBold
                )
            }

            AnimatedVisibility(
                visible = registerState is RegisterState.Error || registerState is RegisterState.Loading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                when (registerState) {
                    is RegisterState.Error -> {
                        Text(
                            text = (registerState as RegisterState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                    RegisterState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.padding(top = 12.dp), color = purple)
                    }
                    else -> Unit
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
