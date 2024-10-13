package ru.sodovaya.mdash.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ru.sodovaya.mdash.ui.interfaces.ScreenTab

@Composable
internal fun RowScope.TabNavigationItem(tab: ScreenTab, icon: ImageVector) {
    val navigator = LocalNavigator.currentOrThrow
    val isSelected = mutableStateOf(navigator.lastItemOrNull == tab)

    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val onSecondaryContainer = MaterialTheme.colorScheme.onSecondaryContainer

    NavigationBarItem(
        selected = false,
        modifier = Modifier.alpha(if (isSelected.value) 1f else 0.6f),
        label = {
            Text(
                text = tab.tabName,
                fontWeight = if (isSelected.value) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = if (isSelected.value) onSurface else onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        onClick = {
            navigator.replace(tab)
        },
        icon = {
            Icon(
                modifier = Modifier.width(24.dp).height(24.dp),
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected.value) onSecondaryContainer else onSurfaceVariant
            )
        }
    )
}