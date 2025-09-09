package mingosgit.josecr.torneoya.ui.screens.usuario.ajustes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.R
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette

private val cardShape = RoundedCornerShape(16.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditosScreen(navController: NavController) {
    val lightText = MaterialTheme.colorScheme.onBackground
    val cardBg = MaterialTheme.colorScheme.surface
    val violet = TorneoYaPalette.violet
    val blue = TorneoYaPalette.blue
    val modernBackground = TorneoYaPalette.backgroundGradient
    val uriHandler = LocalUriHandler.current
    val gradientBluePurple = Brush.horizontalGradient(listOf(blue, violet))

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        GradientBorderedIconButton(
                            icon = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.creditos_volver),
                            onClick = { navController.popBackStack() },
                            gradient = gradientBluePurple
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.creditos_titulo),
                            color = lightText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(modernBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = innerPadding.calculateTopPadding()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, cardShape)
                        .clip(cardShape)
                        .background(cardBg)
                        .border(
                            width = 2.dp,
                            brush = gradientBluePurple,
                            shape = cardShape
                        ),
                    color = cardBg,
                    tonalElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            stringResource(R.string.creditos_creado_por),
                            fontSize = 20.sp,
                            color = lightText,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.creditos_nombre_autor),
                            fontSize = 18.sp,
                            color = lightText,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = stringResource(R.string.creditos_agradecimiento),
                            color = lightText,
                            fontSize = 17.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        CreditLink(
                            text = stringResource(R.string.creditos_github),
                            url = stringResource(R.string.creditos_github_url),
                            gradient = gradientBluePurple,
                            bg = cardBg,
                            textColor = lightText,
                            uriHandler = uriHandler
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        CreditLink(
                            text = stringResource(R.string.creditos_web_personal),
                            url = stringResource(R.string.creditos_web_personal_url),
                            gradient = gradientBluePurple,
                            bg = cardBg,
                            textColor = lightText,
                            uriHandler = uriHandler
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        CreditLink(
                            text = stringResource(R.string.creditos_linkedin),
                            url = stringResource(R.string.creditos_linkedin_url),
                            gradient = gradientBluePurple,
                            bg = cardBg,
                            textColor = lightText,
                            uriHandler = uriHandler
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        CreditLink(
                            text = stringResource(R.string.creditos_email),
                            url = stringResource(R.string.creditos_email_url),
                            gradient = gradientBluePurple,
                            bg = cardBg,
                            textColor = lightText,
                            uriHandler = uriHandler
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreditLink(
    text: String,
    url: String,
    gradient: Brush,
    bg: Color,
    textColor: Color,
    uriHandler: UriHandler
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(
                width = 2.dp,
                brush = gradient,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { uriHandler.openUri(url) }
            .padding(vertical = 14.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
