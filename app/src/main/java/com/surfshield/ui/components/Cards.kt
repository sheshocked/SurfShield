package com.surfshield.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.surfshield.ui.theme.SurfPalette

/**
 * The card and row vocabulary the whole app is built from.
 *
 * The shape is deliberate: an uppercase label sits outside a rounded card, and
 * every row inside leads with a tinted icon tile. That tile is what turns a
 * wall of identical text rows into something scannable - the eye lands on
 * colour and shape before it reads a single word.
 */

/** Small uppercase heading that sits above a card, not inside it. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = SurfPalette.Muted,
        letterSpacing = 1.4.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 10.dp),
    )
}

/** Rounded container that groups related rows. */
@Composable
fun SurfCard(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val border by animateColorAsState(
        if (selected) SurfPalette.Teal else SurfPalette.Outline.copy(alpha = 0.55f),
        tween(260),
        label = "cardBorder",
    )
    val fill by animateColorAsState(
        if (selected) SurfPalette.Teal.copy(alpha = 0.09f) else SurfPalette.Surface,
        tween(260),
        label = "cardFill",
    )

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = fill,
        contentColor = SurfPalette.OnBackground,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(if (selected) 1.6.dp else 1.dp, border, RoundedCornerShape(22.dp)),
    ) {
        Column(Modifier.padding(vertical = 6.dp), content = content)
    }
}

/** Tinted rounded square holding a glyph. */
@Composable
fun IconTile(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Int = 42,
) {
    Box(
        modifier
            .size(size.dp)
            .clip(RoundedCornerShape((size / 3).dp))
            .background(
                Brush.linearGradient(
                    listOf(tint.copy(alpha = 0.22f), tint.copy(alpha = 0.10f))
                )
            )
            .border(1.dp, tint.copy(alpha = 0.28f), RoundedCornerShape((size / 3).dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size((size * 0.5).dp))
    }
}

/** Divider inset so it starts where the text does, not under the icon. */
@Composable
fun RowDivider() {
    Box(
        Modifier
            .padding(start = 74.dp, end = 18.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(SurfPalette.Outline.copy(alpha = 0.5f))
    )
}

/**
 * Navigational row: icon, title, optional subtitle, trailing value, chevron.
 */
@Composable
fun IconRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    value: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val interaction = remember { MutableInteractionSource() }

    Row(
        modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier
                        .pressScale(interaction, pressedScale = 0.985f)
                        .clickable(interactionSource = interaction, indication = null) { onClick() }
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(icon, tint)
        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = SurfPalette.OnBackground,
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = SurfPalette.Muted,
                )
            }
        }

        if (value != null) {
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                color = SurfPalette.Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 8.dp, end = 6.dp),
            )
        }

        if (onClick != null) {
            Icon(
                SurfIcons.ChevronRight,
                contentDescription = null,
                tint = SurfPalette.Muted.copy(alpha = 0.8f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/** Switch row that also leads with an icon tile. */
@Composable
fun IconSwitchRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    onChange: (Boolean) -> Unit,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(icon, if (enabled) tint else SurfPalette.Muted)
        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) SurfPalette.OnBackground else SurfPalette.Muted,
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = SurfPalette.Muted,
                )
            }
        }

        Spacer(Modifier.width(10.dp))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SurfPalette.Navy,
                checkedTrackColor = SurfPalette.Teal,
                uncheckedThumbColor = SurfPalette.Muted,
                uncheckedTrackColor = SurfPalette.SurfaceElevated,
                uncheckedBorderColor = SurfPalette.Outline,
            ),
        )
    }
}

/** Row of colour dots previewing a palette, with a check when selected. */
@Composable
fun SwatchDots(
    colors: List<Color>,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        colors.forEach { colour ->
            Box(
                Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(colour)
                    .border(1.dp, SurfPalette.Outline.copy(alpha = 0.7f), CircleShape)
            )
        }
        if (selected) {
            Spacer(Modifier.width(2.dp))
            Box(
                Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(SurfPalette.Teal),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    SurfIcons.Check,
                    contentDescription = null,
                    tint = SurfPalette.Navy,
                    modifier = Modifier.size(13.dp),
                )
            }
        }
    }
}

/** The three root destinations. */
enum class NavTab(val label: String, val icon: ImageVector) {
    HOME("Home", SurfIcons.Home),
    SERVERS("Servers", SurfIcons.Servers),
    SETTINGS("Settings", SurfIcons.Sliders),
}

/**
 * Floating bottom bar. The selected tab keeps a tinted pill behind it, which
 * animates between destinations rather than snapping.
 */
@Composable
fun SurfBottomBar(
    current: NavTab,
    modifier: Modifier = Modifier,
    onSelect: (NavTab) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = SurfPalette.SurfaceElevated.copy(alpha = 0.96f),
        contentColor = SurfPalette.OnBackground,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .border(1.dp, SurfPalette.Outline.copy(alpha = 0.7f), RoundedCornerShape(26.dp)),
    ) {
        Row(
            Modifier.padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            NavTab.entries.forEach { tab ->
                val active = tab == current
                val background by animateColorAsState(
                    if (active) SurfPalette.Teal.copy(alpha = 0.16f) else Color.Transparent,
                    tween(240),
                    label = "tabFill",
                )
                val content by animateColorAsState(
                    if (active) SurfPalette.Teal else SurfPalette.Muted,
                    tween(240),
                    label = "tabTint",
                )

                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(background)
                        .clickable { onSelect(tab) }
                        .padding(vertical = 9.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        tab.icon,
                        contentDescription = tab.label,
                        tint = content,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        tab.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = content,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}
