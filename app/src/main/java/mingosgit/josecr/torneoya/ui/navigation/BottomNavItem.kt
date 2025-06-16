package mingosgit.josecr.torneoya.ui.navigation

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem("home", "Home", Icons.Filled.Home)
    object Partido : BottomNavItem("partido", "Partido", Icons.Filled.SportsSoccer)
    object Usuario : BottomNavItem("usuario", "Usuario", Icons.Filled.Person)
}
