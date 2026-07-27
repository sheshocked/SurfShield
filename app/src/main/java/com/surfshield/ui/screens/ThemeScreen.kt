package com.surfshield.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.surfshield.data.AppSettings
import com.surfshield.data.ThemeMode
import com.surfshield.ui.components.SurfIcons
import com.surfshield.ui.theme.ColorPaletteName
import com.surfshield.ui.theme.SurfPalette

@Composable
fun ThemeScreen(
    settings: AppSettings,
    @Suppress("UNUSED_PARAMETER") revision: Long,
    onBack: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Custom INCY style Header
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "\u2190",
                fontSize = 24.sp,
                color = SurfPalette.OnBackground,
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .padding(end = 16.dp)
            )
            Text(
                "Theme",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = SurfPalette.OnBackground,
                modifier = Modifier.weight(1f)
            )
            Text(
                "Done",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = SurfPalette.Teal,
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        Divider(color = SurfPalette.Outline, thickness = 1.dp)

        // INCY style connection status preview card
        Box(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
            Column {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "SURFSHIELD",
                        color = SurfPalette.Teal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(SurfPalette.Indigo.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(SurfPalette.Teal)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Protected",
                                color = SurfPalette.OnBackground,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SurfPalette.Teal.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = SurfIcons.Shield,
                            contentDescription = null,
                            tint = SurfPalette.Teal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Connected",
                            color = SurfPalette.OnBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            "Secure tunnel active",
                            color = SurfPalette.Muted,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        "\uD83C\uDDF3\uD83C\uDDF1", // NL Flag
                        fontSize = 28.sp
                    )
                }
            }
        }

        // Palette selector list
        Text(
            "CHOOSE PALETTE",
            style = MaterialTheme.typography.bodySmall,
            color = SurfPalette.Teal,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 8.dp)
        )

        val palettes = listOf(
            Triple(ColorPaletteName.MIDNIGHT, "Midnight", "Default cyan/blue theme"),
            Triple(ColorPaletteName.AURORA, "Aurora", "Purple dream theme"),
            Triple(ColorPaletteName.EMBER, "Ember", "Warm orange accent"),
            Triple(ColorPaletteName.NORD, "Nord", "Cool ice blue theme"),
            Triple(ColorPaletteName.FOREST, "Forest", "Deep wood green"),
            Triple(ColorPaletteName.MONO, "Mono", "Elegant grey style")
        )

        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            palettes.forEach { (pal, name, desc) ->
                val selected = settings.colorPalette == pal
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { settings.colorPalette = pal }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            name,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) SurfPalette.Teal else SurfPalette.OnBackground
                        )
                        Text(
                            desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = SurfPalette.Muted
                        )
                    }
                    if (selected) {
                        Text(
                            "\u2713", // Checkmark
                            color = SurfPalette.Teal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // Theme tweaks card
        Text(
            "THEME TWEAKS",
            style = MaterialTheme.typography.bodySmall,
            color = SurfPalette.Teal,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 8.dp)
        )

        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
            Column {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "AMOLED black",
                            fontWeight = FontWeight.Bold,
                            color = SurfPalette.OnBackground
                        )
                        Text(
                            "Pure black theme background for saving battery",
                            style = MaterialTheme.typography.bodySmall,
                            color = SurfPalette.Muted
                        )
                    }
                    val amoled = settings.themeMode == ThemeMode.AMOLED
                    Switch(
                        checked = amoled,
                        onCheckedChange = {
                            settings.themeMode = if (it) ThemeMode.AMOLED else ThemeMode.DARK
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}
