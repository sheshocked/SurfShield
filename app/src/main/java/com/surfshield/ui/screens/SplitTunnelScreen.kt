package com.surfshield.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.surfshield.data.AppEntry
import com.surfshield.data.AppSettings
import com.surfshield.data.InstalledApps
import com.surfshield.data.SplitTunnelMode
import com.surfshield.ui.components.SegmentedRow
import com.surfshield.ui.theme.SurfPalette

@Composable
fun SplitTunnelScreen(
    settings: AppSettings,
    revision: Long,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<AppEntry>?>(null) }
    var showSystem by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    LaunchedEffect(showSystem) {
        apps = null
        apps = InstalledApps.load(context, includeSystem = showSystem)
    }

    val mode = settings.splitTunnelMode
    val selected = settings.splitTunnelApps

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "\u2039",
                style = MaterialTheme.typography.displaySmall,
                color = SurfPalette.Muted,
                modifier = Modifier.clickable(onClick = onBack).padding(end = 14.dp),
            )
            Text("Split tunnelling", style = MaterialTheme.typography.headlineSmall)
        }

        SegmentedRow(
            title = "Mode",
            subtitle = when (mode) {
                SplitTunnelMode.OFF -> "Every app uses the tunnel"
                SplitTunnelMode.EXCLUDE -> "Selected apps bypass the tunnel and use your real connection"
                SplitTunnelMode.INCLUDE -> "Only selected apps use the tunnel"
            },
            options = SplitTunnelMode.entries.toList(),
            selected = mode,
            label = { it.name.lowercase() },
        ) { settings.splitTunnelMode = it }

        if (mode != SplitTunnelMode.OFF) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search apps", color = SurfPalette.Muted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { showSystem = !showSystem }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = showSystem, onCheckedChange = { showSystem = it })
                Text("Show system apps", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.weight(1f))
                Text(
                    "${selected.size} selected",
                    style = MaterialTheme.typography.bodySmall,
                    color = SurfPalette.Teal,
                )
            }

            val list = apps
            if (list == null) {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) { CircularProgressIndicator(color = SurfPalette.Teal) }
            } else {
                val filtered = remember(list, query) {
                    if (query.isBlank()) list
                    else list.filter {
                        it.label.contains(query, true) || it.packageName.contains(query, true)
                    }
                }

                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(filtered, key = { it.packageName }) { app ->
                        val checked = app.packageName in selected
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (checked) SurfPalette.Teal.copy(alpha = 0.08f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable {
                                    settings.splitTunnelApps =
                                        if (checked) selected - app.packageName
                                        else selected + app.packageName
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val icon = app.icon
                            if (icon != null) {
                                Image(
                                    bitmap = remember(app.packageName) {
                                        icon.toBitmap(96, 96).asImageBitmap()
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                )
                            } else {
                                Spacer(Modifier.size(36.dp))
                            }
                            Spacer(Modifier.size(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(app.label, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    app.packageName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SurfPalette.Muted,
                                )
                            }
                            Checkbox(checked = checked, onCheckedChange = null)
                        }
                    }

                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}
