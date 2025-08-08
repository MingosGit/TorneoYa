package mingosgit.josecr.torneoya.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import mingosgit.josecr.torneoya.R

sealed class BottomNavItem(
    val route: String,
    @StringRes val titleResId: Int,
    val icon: ImageVector
) {
    data object Home : BottomNavItem("home", R.string.BottomNav_Home, Icons.Filled.Home)
    data object Online : BottomNavItem("partidos_online", R.string.BottomNav_Online, Icons.Filled.LiveTv)
    data object Amigos : BottomNavItem("amigos", R.string.BottomNav_Amigos, Icons.Filled.Group)
    data object Usuario : BottomNavItem("usuario", R.string.BottomNav_Usuario, Icons.Filled.Person)
}
