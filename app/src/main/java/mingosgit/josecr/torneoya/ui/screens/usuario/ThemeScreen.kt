package mingosgit.josecr.torneoya.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.R

private val cardShape = RoundedCornerShape(16.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeScreen(
    navController: NavController,
    currentThemeDark: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    var selectedDark by rememberSaveable { mutableStateOf(currentThemeDark) }

    LaunchedEffect(currentThemeDark) {
        selectedDark = currentThemeDark
    }

    val colorScheme = MaterialTheme.colorScheme
    val modernBackground = TorneoYaPalette.backgroundGradient

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
                            contentDescription = "Volver",
                            onClick = { navController.popBackStack() },
                            gradient = Brush.horizontalGradient(listOf(colorScheme.primary, colorScheme.secondary))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(id = R.string.ajustes_tema_app),
                            color = colorScheme.onBackground,
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
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    start = 14.dp,
                    end = 14.dp
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ThemeOptionCard(
                    title = stringResource(id = R.string.theme_light),
                    selected = !selectedDark,
                    onClick = {
                        if (selectedDark) {
                            selectedDark = false
                            onThemeChange(false)
                        }
                    },
                    colorScheme = colorScheme
                )
                ThemeOptionCard(
                    title = stringResource(id = R.string.theme_dark),
                    selected = selectedDark,
                    onClick = {
                        if (!selectedDark) {
                            selectedDark = true
                            onThemeChange(true)
                        }
                    },
                    colorScheme = colorScheme
                )
            }
        }
    }
}

@Composable
private fun ThemeOptionCard(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    colorScheme: ColorScheme
) {
    val leftColor = colorScheme.primary.copy(alpha = 0.8f)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, cardShape)
            .clip(cardShape)
            .background(colorScheme.surfaceVariant)
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(listOf(leftColor, colorScheme.secondary)),
                shape = cardShape
            )
            .clickable { onClick() },
        color = colorScheme.surfaceVariant,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 22.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 17.sp,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun GradientBorderedIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    gradient: Brush,
    size: androidx.compose.ui.unit.Dp = 38.dp,
    iconSize: androidx.compose.ui.unit.Dp = 21.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .border(
                width = 2.5.dp,
                brush = gradient,
                shape = CircleShape
            )
            .background(Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(iconSize)
        )
    }
}
