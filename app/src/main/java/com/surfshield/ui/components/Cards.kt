package com.surfshield.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.surfshield.ui.theme.SurfPalette

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.bodySmall,
        color = SurfPalette.Teal,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(start = 8.dp, bottom = 8.dp, top = 16.dp)
    )
}

@Composable
fun SurfCard(
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val borderModifier = if (isSelected) {
        Modifier.border(1.5.dp, SurfPalette.Teal, RoundedCornerShape(22.dp))
    } else {
        Modifier.border(1.dp, SurfPalette.Outline, RoundedCornerShape(22.dp))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(borderModifier)
            .padding(16.dp),
        content = content
    )
}

@Composable
fun IconTile(
    icon: ImageVector,
    gradient: List<Color> = listOf(SurfPalette.Teal, SurfPalette.Cyan),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.linearGradient(gradient)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SurfPalette.Navy,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun IconRow(
    title: String,
    icon: ImageVector,
    value: String? = null,
    subtitle: String? = null,
    gradient: List<Color> = listOf(SurfPalette.Teal, SurfPalette.Cyan),
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(icon = icon, gradient = gradient)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = SurfPalette.OnBackground)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SurfPalette.Muted)
            }
        }
        if (value != null) {
            Text(value, style = MaterialTheme.typography.bodyMedium, color = SurfPalette.Teal)
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(" \u203a", color = SurfPalette.Muted, fontSize = 20.sp)
    }
}

@Composable
fun IconSwitchRow(
    title: String,
    checked: Boolean,
    icon: ImageVector,
    subtitle: String? = null,
    gradient: List<Color> = listOf(SurfPalette.Teal, SurfPalette.Cyan),
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(icon = icon, gradient = gradient)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = SurfPalette.OnBackground)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SurfPalette.Muted)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SwatchDots(colors: List<Color>, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        colors.take(3).forEach { color ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
