package com.surfshield.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.surfshield.ui.theme.SurfPalette
import kotlin.math.roundToInt

/**
 * Shared settings rows.
 *
 * Every row states its text colour explicitly. This is not redundant: the group
 * card is a Surface so it does propagate a content colour, but several of these
 * rows are also used outside a group, and relying on ambient inheritance is
 * exactly how the titles ended up rendering black on a dark background.
 */

/** Card wrapper that groups related settings under a heading. */
@Composable
fun SettingsGroup(
    title: String,
    modifier: Modifier = Modifier,
    footnote: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.bodySmall,
            color = SurfPalette.Teal,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 8.dp, bottom = 6.dp),
        )

        // Surface rather than Modifier.background: it supplies contentColor to
        // everything inside, so nested text defaults to a readable colour.
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = SurfPalette.SurfaceElevated,
            contentColor = SurfPalette.OnBackground,
        ) {
            Column(content = content)
        }

        if (footnote != null) {
            Text(
                footnote,
                style = MaterialTheme.typography.bodySmall,
                color = SurfPalette.Muted,
                modifier = Modifier.padding(start = 8.dp, top = 6.dp, end = 8.dp),
            )
        }
    }
}

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
                color = if (enabled) SurfPalette.OnBackground else SurfPalette.Muted,
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SurfPalette.Muted)
            }
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SurfPalette.Navy,
                checkedTrackColor = SurfPalette.Teal,
                uncheckedThumbColor = SurfPalette.Muted,
                uncheckedTrackColor = SurfPalette.Outline,
            ),
        )
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
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = SurfPalette.OnBackground,
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
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
        Text(
            "  >",
            style = MaterialTheme.typography.bodyMedium,
            color = SurfPalette.Muted,
        )
    }
}

/** Tappable row that performs an action. [danger] marks destructive ones. */
@Composable
fun ActionRow(
    title: String,
    subtitle: String? = null,
    danger: Boolean = false,
    onClick: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = if (danger) SurfPalette.Danger else SurfPalette.Teal,
        )
        if (subtitle != null) {
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SurfPalette.Muted)
        }
    }
}

/**
 * Wrapping chip row for enum choices.
 *
 * FlowRow, not Row: with seven obfuscation modes a fixed Row squeezed the later
 * chips until their labels wrapped mid-word. Labels are pinned to one line and
 * the row wraps instead.
 */
@OptIn(ExperimentalLayoutApi::class)
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
        Text(title, style = MaterialTheme.typography.titleMedium, color = SurfPalette.OnBackground)
        if (subtitle != null) {
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SurfPalette.Muted)
        }
        Spacer(Modifier.height(10.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { option ->
                val active = option == selected
                Text(
                    text = label(option),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (active) SurfPalette.Navy else SurfPalette.OnBackground,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (active) SurfPalette.Teal else SurfPalette.Surface)
                        .clickable { onSelect(option) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
    }
}

/** Single-choice row. Used for DNS presets, where the value matters enough to show. */
@Composable
fun RadioRow(
    title: String,
    selected: Boolean,
    subtitle: String? = null,
    onSelect: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(
                selectedColor = SurfPalette.Teal,
                unselectedColor = SurfPalette.Muted,
            ),
        )
        Spacer(Modifier.width(4.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = SurfPalette.OnBackground,
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = SurfPalette.Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * Slider that commits on release.
 *
 * The previous version pushed every intermediate drag value straight into
 * SharedPreferences and then re-read it as its own input. Combined with an
 * out-of-range sentinel value that meant "automatic", the thumb appeared frozen
 * at the minimum and no change ever stuck. Local drag state fixes both.
 */
@Composable
fun SliderRow(
    title: String,
    value: Int,
    range: IntRange,
    steps: Int = 0,
    enabled: Boolean = true,
    valueLabel: (Int) -> String = { it.toString() },
    subtitle: String? = null,
    onChange: (Int) -> Unit,
) {
    var dragValue by remember { mutableStateOf<Float?>(null) }
    val shown = (dragValue ?: value.toFloat())
        .coerceIn(range.first.toFloat(), range.last.toFloat())

    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) SurfPalette.OnBackground else SurfPalette.Muted,
                modifier = Modifier.weight(1f),
            )
            Text(
                valueLabel(shown.roundToInt()),
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) SurfPalette.Teal else SurfPalette.Muted,
            )
        }
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SurfPalette.Muted)
        }
        Slider(
            value = shown,
            enabled = enabled,
            onValueChange = { dragValue = it },
            onValueChangeFinished = {
                dragValue?.let { onChange(it.roundToInt()) }
                dragValue = null
            },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = SurfPalette.Teal,
                activeTrackColor = SurfPalette.Teal,
                inactiveTrackColor = SurfPalette.Outline,
                disabledThumbColor = SurfPalette.Muted,
                disabledActiveTrackColor = SurfPalette.Outline,
            ),
        )
    }
}

@Composable
fun TextFieldRow(
    title: String,
    value: String,
    placeholder: String = "",
    subtitle: String? = null,
    error: String? = null,
    visible: Boolean = true,
    onChange: (String) -> Unit,
) {
    AnimatedVisibility(visible) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = SurfPalette.OnBackground,
            )
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SurfPalette.Muted)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onChange,
                placeholder = { Text(placeholder, color = SurfPalette.Muted) },
                singleLine = true,
                isError = error != null,
                modifier = Modifier.fillMaxWidth(),
            )
            if (error != null) {
                Spacer(Modifier.height(4.dp))
                Text(error, style = MaterialTheme.typography.bodySmall, color = SurfPalette.Danger)
            }
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
        maxLines = 1,
        softWrap = false,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
