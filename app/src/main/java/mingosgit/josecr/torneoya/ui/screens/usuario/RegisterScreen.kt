package mingosgit.josecr.torneoya.ui.screens.usuario

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.R
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.viewmodel.usuario.RegisterState
import mingosgit.josecr.torneoya.viewmodel.usuario.RegisterViewModel
import java.util.Locale

private const val MIN_PASSWORD_LENGTH = 6
private const val PRIVACY_URL = "https://mingosgit.github.io/privacy-policy.html"
private const val PRIVACY_VERSION = "2025-08-11"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    registerViewModel: RegisterViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nombreUsuario by remember { mutableStateOf("") }
    var aceptoPrivacidad by remember { mutableStateOf(false) }

    val registerState by registerViewModel.registerState.collectAsState()
    var navegarAConfirmarCorreo by remember { mutableStateOf(false) }

    val blue = TorneoYaPalette.blue
    val violet = TorneoYaPalette.violet
    val backgroundBrush = TorneoYaPalette.backgroundGradient

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

    val passwordErrorResId = remember(password) {
        when {
            password.isEmpty() -> null
            password.length < MIN_PASSWORD_LENGTH -> R.string.auth_password_min_length
            !password.any { it.isUpperCase() } -> R.string.auth_password_need_uppercase
            !password.any { it.isLowerCase() } -> R.string.auth_password_need_lowercase
            !password.any { it.isDigit() } -> R.string.auth_password_need_digit
            else -> null
        }
    }

    val canRegister = registerState != RegisterState.Loading &&
            email.isNotBlank() &&
            nombreUsuario.isNotBlank() &&
            passwordErrorResId == null &&
            aceptoPrivacidad

    val uriHandler = LocalUriHandler.current
    val linkText: AnnotatedString = buildAnnotatedString {
        append(stringResource(id = R.string.register_privacy_prefix) + " ")
        pushStringAnnotation(tag = "URL", annotation = PRIVACY_URL)
        withStyle(
            SpanStyle(
                color = blue,
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.SemiBold
            )
        ) {
            append(stringResource(id = R.string.register_privacy_link))
        }
        pop()
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
                                    .background(
                                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                                        CircleShape
                                    )
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
                color = violet.copy(alpha = 0.10f),
                shadowElevation = 0.dp,
                modifier = Modifier.size(74.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = stringResource(id = R.string.register_crear_cuenta),
                    tint = violet,
                    modifier = Modifier
                        .padding(18.dp)
                        .fillMaxSize()
                )
            }

            Spacer(Modifier.height(15.dp))

            Text(
                text = stringResource(id = R.string.register_crear_cuenta),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.register_unete_comunidad),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(28.dp))

            OutlinedTextField(
                value = nombreUsuario,
                onValueChange = { nombreUsuario = it.trim() },
                label = { Text(stringResource(id = R.string.register_nombre_usuario_label), color = violet) },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = violet)
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
                    focusedBorderColor = violet,
                    unfocusedBorderColor = violet.copy(alpha = 0.6f),
                    cursorColor = violet,
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
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        ),
                        RoundedCornerShape(17.dp)
                    ),
                visualTransformation = PasswordVisualTransformation(),
                supportingText = {
                    passwordErrorResId?.let { resId ->
                        val msg =
                            if (resId == R.string.auth_password_min_length)
                                stringResource(resId, MIN_PASSWORD_LENGTH)
                            else
                                stringResource(resId)
                        Text(text = msg, color = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.Transparent,
                    focusedBorderColor = if (passwordErrorResId == null) blue else MaterialTheme.colorScheme.error,
                    unfocusedBorderColor = if (passwordErrorResId == null) blue.copy(alpha = 0.6f) else MaterialTheme.colorScheme.error,
                    cursorColor = blue,
                )
            )

            Spacer(Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = aceptoPrivacidad,
                    onCheckedChange = { aceptoPrivacidad = it }
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = linkText,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.clickable {
                        try {
                            uriHandler.openUri(PRIVACY_URL)
                        } catch (_: Throwable) {
                            val i = Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_URL))
                            context.startActivity(i)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(listOf(violet, blue)),
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
                    .clickable(enabled = canRegister) {
                        registerViewModel.register(
                            email = email,
                            password = password,
                            nombreUsuario = nombreUsuario,
                            acceptedPrivacy = aceptoPrivacidad,
                            privacyVersion = PRIVACY_VERSION,
                            privacyUrl = PRIVACY_URL
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(id = R.string.register_registrar_button),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = if (canRegister)
                        MaterialTheme.colorScheme.onBackground
                    else
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
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
                visible = registerState is RegisterState.Error
                        || registerState is RegisterState.Success
                        || registerState is RegisterState.Loading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                when (registerState) {
                    is RegisterState.Error -> {
                        val res = (registerState as RegisterState.Error).resId
                        Text(
                            text = stringResource(id = res),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                    RegisterState.Success -> {
                        Text(
                            text = stringResource(id = R.string.register_success_message),
                            color = violet,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                        LaunchedEffect(Unit) {
                            navegarAConfirmarCorreo = true
                        }
                    }
                    RegisterState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(top = 12.dp),
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
