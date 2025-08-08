package mingosgit.josecr.torneoya.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Online,
        BottomNavItem.Amigos,
        BottomNavItem.Usuario
    )
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 12.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .background(MaterialTheme.colorScheme.surface),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                val color by animateColorAsState(
                    if (selected) MaterialTheme.colorScheme.primary else Color.Gray,
                    label = "nav-item"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            if (!selected) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                        .padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(item.titleResId),
                        tint = color,
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        text = stringResource(item.titleResId),
                        color = color,
                        fontSize = if (selected) 13.sp else 12.sp,
                        style = if (selected) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 3.dp)
                            .height(if (selected) 3.dp else 0.dp)
                            .width(32.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
                            )
                    )
                }
            }
        }
    }
}
