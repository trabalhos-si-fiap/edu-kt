package br.com.edu.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import br.com.edu.core.theme.EduColors

@Composable
fun AuthBottomBar(
    selected: Int,
    onTabSelected: (Int) -> Unit,
) {
    Surface(
        color = EduColors.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        ) {
            authItems.forEachIndexed { index, item ->
                NavigationBarItem(
                    selected = selected == index,
                    onClick = { onTabSelected(index) },
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    label = { Text(item.label) },
                    colors = navItemColors(),
                )
            }
        }
    }
}

@Composable
fun MainBottomBar(
    selected: Int,
    onTabSelected: (Int) -> Unit,
) {
    Surface(
        color = EduColors.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        ) {
            mainItems.forEachIndexed { index, item ->
                NavigationBarItem(
                    selected = selected == index,
                    onClick = { onTabSelected(index) },
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    label = { Text(item.label) },
                    colors = navItemColors(),
                )
            }
        }
    }
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = EduColors.Purple,
    selectedTextColor = EduColors.Purple,
    unselectedIconColor = EduColors.TextSecondary,
    unselectedTextColor = EduColors.TextSecondary,
    indicatorColor = EduColors.PurpleSoft,
)

private data class NavItem(val label: String, val icon: ImageVector)

private val authItems = listOf(
    NavItem("Entrar", Icons.Outlined.Login),
    NavItem("Cadastro", Icons.Outlined.PersonAdd),
)

private val mainItems = listOf(
    NavItem("Loja", Icons.Outlined.Storefront),
    NavItem("Meus Pedidos", Icons.Outlined.ReceiptLong),
)
