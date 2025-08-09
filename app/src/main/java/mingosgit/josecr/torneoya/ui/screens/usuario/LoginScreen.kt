package mingosgit.josecr.torneoya.ui.screens.usuario

import android.app.Activity
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.viewmodel.usuario.GlobalUserViewModel
import mingosgit.josecr.torneoya.viewmodel.usuario.LoginState
import mingosgit.josecr.torneoya.viewmodel.usuario.LoginViewModel
import mingosgit.josecr.torneoya.viewmodel.usuario.ResetPasswordState
import java.util.Locale

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

    // Paleta y colores alineados con UsuarioScreen/ModernTorneoYaTheme
    val modernBackground = TorneoYaPalette.backgroundGradient
    val blue = TorneoYaPalette.blue
    val violet = TorneoYaPalette.violet

    val enterEnabled = loginState != LoginState.Loading && email.isNotBlank() && password.length >= 6
    val resetEnabled = email.isNotBlank() && resetPasswordState != ResetPasswordState.Loading

    val loginTitle = stringResource(id = R.string.login_title)
    val loginSubtitle = stringResource(id = R.string.login_subtitle)
    val emailLabel = stringResource(id = R.string.login_email_label)
    val passwordLabel = stringResource(id = R.string.login_password_label)
    val enterButtonText = stringResource(id = R.string.login_button_enter)
    val noAccountText = stringResource(id = R.string.login_no_account)
    val forgotPasswordText = stringResource(id = R.string.login_forgot_password)
    val loginSuccessMessage = stringResource(id = R.string.login_success_message)
    val resetSuccessMessage = stringResource(id = R.string.login_reset_success)

    val context = LocalContext.current
    val currentLocale = remember { mutableStateOf(Locale.getDefault().language) }
    val languageCodes = listOf("es", "ca", "en")
    val banderas = listOf(
        R.drawable.flag_es,
        R.drawable.flag_cat,
        R.drawable.flag_uk
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(modernBackground)
    ) {
        // Selector de idioma
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
                                brush = Brush.horizontalGradient(listOf(blue, violet)),
                                shape = CircleShape
                            )
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.surface,
                                        MaterialTheme.colorScheme.surfaceVariant
                                    )
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
                                    .background(Color.Black.copy(alpha = 0.2f), CircleShape)
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
            Spacer(Modifier.height(16.dp))

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.13f),
                shadowElevation = 0.dp,
                modifier = Modifier.size(76.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = stringResource(id = R.string.gen_iniciar_sesion),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(17.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = loginTitle,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = loginSubtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(Modifier.height(30.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it.trim() },
                label = { Text(emailLabel, color = MaterialTheme.colorScheme.primary) },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.MailOutline,
                        contentDescription = emailLabel,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        ),
                        RoundedCornerShape(17.dp)
                    ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.Transparent,
                    focusedBorderColor = blue,
                    unfocusedBorderColor = blue.copy(alpha = 0.65f),
                    cursorColor = blue,
                    focusedLabelColor = blue,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedLeadingIconColor = blue,
                    unfocusedLeadingIconColor = blue
                )
            )

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(passwordLabel, color = MaterialTheme.colorScheme.secondary) },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = passwordLabel,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        ),
                        RoundedCornerShape(17.dp)
                    ),
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.Transparent,
                    focusedBorderColor = violet,
                    unfocusedBorderColor = violet.copy(alpha = 0.65f),
                    cursorColor = violet,
                    focusedLabelColor = violet,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedLeadingIconColor = violet,
                    unfocusedLeadingIconColor = violet
                )
            )

            Spacer(Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(listOf(blue, violet)),
                        shape = RoundedCornerShape(15.dp)
                    )
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    )
                    .clickable(enabled = enterEnabled) {
                        loginViewModel.login(email, password)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    enterButtonText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = if (enterEnabled) MaterialTheme.colorScheme.onBackground
                    else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
            }

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(listOf(blue, violet)),
                        shape = RoundedCornerShape(15.dp)
                    )
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    )
                    .clickable {
                        loginViewModel.clearState()
                        navController.navigate("register")
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    noAccountText,
                    color = blue,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(4.dp))

            TextButton(
                onClick = { loginViewModel.enviarCorreoRestablecerPassword(email) },
                enabled = resetEnabled,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = violet,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    forgotPasswordText,
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
                            text = loginSuccessMessage,
                            color = blue,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                        val nombreUsuarioOnline = (loginState as LoginState.Success).usuario.nombreUsuario
                        LaunchedEffect(Unit) {
                            globalUserViewModel.setNombreUsuarioOnline(nombreUsuarioOnline)
                            // ⬇️ Evitar llamada a reiniciarApp() que falla por nombre manglado
                            navController.navigate("usuario") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                    LoginState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(top = 12.dp),
                            color = blue
                        )
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
                            text = resetSuccessMessage,
                            color = violet,
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
                        CircularProgressIndicator(
                            modifier = Modifier.padding(top = 8.dp),
                            color = violet
                        )
                    }
                    else -> Unit
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
