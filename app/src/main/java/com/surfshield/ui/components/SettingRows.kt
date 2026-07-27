package com.surfshield.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.surfshield.ui.theme.SurfPalette

/** Card wrapper that groups related settings under a heading. */
@Composable
fun SettingsGroup(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnContent,
) {
    Column(modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.bodySmall,
            color = SurfPalette.Teal,
            modifier = Modifier.padding(start = 8.dp, bottom = 6.dp),
        )
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) { content() }
    }
}

typealias ColumnContent = @Composable () -> Unit

@Composable
fun SwitchRow(
    title: String,
    checked: Boolean,
    subtitle: String? = null,
    enabled: Boolean = true,
    onChange: (Boolean) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else SurfPalette.Muted,
            )
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SurfPalette.Muted)
            }
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onChange, enabled = enabled)
    }
}

@Composable
fun NavigationRow(
    title: String,
    value: String? = null,
    subtitle: String? = null,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SurfPalette.Muted)
            }
        }
        if (value != null) {
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                color = SurfPalette.Teal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Text("  \u203A", color = SurfPalette.Muted)
    }
}

/** Horizontal chip row for small enum choices. */
@Composable
fun <T> SegmentedRow(
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    subtitle: String? = null,
    onSelect: (T) -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SurfPalette.Muted)
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                val active = option == selected
                Text(
                    text = label(option),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (active) SurfPalette.Navy else SurfPalette.OnBackground,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (active) SurfPalette.Teal
                            else MaterialTheme.colorScheme.surface
                        )
                        .clickable { onSelect(option) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
fun SliderRow(
    title: String,
    value: Int,
    range: IntRange,
    steps: Int = 0,
    valueLabel: (Int) -> String = { it.toString() },
    subtitle: String? = null,
    onChange: (Int) -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text(valueLabel(value), style = MaterialTheme.typography.bodyMedium, color = SurfPalette.Teal)
        }
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SurfPalette.Muted)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = steps,
        )
    }
}

@Composable
fun TextFieldRow(
    title: String,
    value: String,
    placeholder: String = "",
    subtitle: String? = null,
    visible: Boolean = true,
    onChange: (String) -> Unit,
) {
    AnimatedVisibility(visible) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SurfPalette.Muted)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onChange,
                placeholder = { Text(placeholder, color = SurfPalette.Muted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Latency badge used in the server list. */
@Composable
fun PingBadge(rttMs: Int?, modifier: Modifier = Modifier) {
    val (text, color) = when {
        rttMs == null -> "--" to SurfPalette.Muted
        rttMs == Int.MAX_VALUE -> "blocked" to SurfPalette.Danger
        rttMs < 120 -> "$rttMs ms" to SurfPalette.Success
        rttMs < 300 -> "$rttMs ms" to SurfPalette.Warning
        else -> "$rttMs ms" to SurfPalette.Danger
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = color,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
