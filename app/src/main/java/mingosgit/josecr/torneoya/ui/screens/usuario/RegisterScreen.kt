package mingosgit.josecr.torneoya.ui.screens.usuario

import android.app.Activity
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.R
import mingosgit.josecr.torneoya.viewmodel.usuario.RegisterViewModel
import mingosgit.josecr.torneoya.viewmodel.usuario.RegisterState
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import java.util.Locale

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

    val context = LocalContext.current
    val currentLocale = remember { mutableStateOf(Locale.getDefault().language) }
    val languageCodes = listOf("es", "ca", "en")
    val banderas = listOf(
        R.drawable.flag_es,
        R.drawable.flag_cat,
        R.drawable.flag_uk
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Top
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                languageCodes.forEachIndexed { index, code ->
                    val seleccionado = currentLocale.value == code
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(
                                width = 2.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                                ),
                                shape = CircleShape
                            )
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                                )
                            )
                            .clickable {
                                val sharedPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                                sharedPref.edit().putString("app_language", code).apply()
                                currentLocale.value = code
                                (context as? Activity)?.recreate()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = banderas[index]),
                            contentDescription = "Idioma $code",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                        if (seleccionado) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                            )
                        }
                    }
                }
            }
        }

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
                    contentDescription = stringResource(id = R.string.register_crear_cuenta),
                    tint = purple,
                    modifier = Modifier
                        .padding(18.dp)
                        .fillMaxSize()
                )
            }

            Spacer(Modifier.height(15.dp))

            Text(
                text = stringResource(id = R.string.register_crear_cuenta),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.register_unete_comunidad),
                color = Color(0xFFB7B7D1),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(28.dp))

            OutlinedTextField(
                value = nombreUsuario,
                onValueChange = { nombreUsuario = it.trim() },
                label = { Text(stringResource(id = R.string.register_nombre_usuario_label), color = purple) },
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
                label = { Text(stringResource(id = R.string.register_email_label), color = blue) },
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
                label = { Text(stringResource(id = R.string.register_contraseña_label), color = blue) },
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
                    stringResource(id = R.string.register_registrar_button),
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
                    stringResource(id = R.string.register_ya_tienes_cuenta),
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
