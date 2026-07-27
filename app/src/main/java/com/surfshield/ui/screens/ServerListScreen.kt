package com.surfshield.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.surfshield.data.SurfLocation
import com.surfshield.ui.components.PingBadge
import com.surfshield.ui.theme.SurfPalette

@Composable
fun ServerListScreen(
    locations: List<SurfLocation>,
    pings: Map<String, Int>,
    selectedId: String?,
    smartConnectEnabled: Boolean,
    onSelect: (SurfLocation) -> Unit,
    onSmartConnect: () -> Unit,
    onRefreshPings: () -> Unit,
    onBack: () -> Unit,
) {
    var query by remember { mutableStateOf("") }

    val filtered = remember(locations, query) {
        if (query.isBlank()) locations
        else locations.filter {
            it.country.contains(query, true) ||
                it.city.contains(query, true) ||
                it.id.contains(query, true) ||
                it.ip.contains(query)
        }
    }

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
            Text("Servers", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            Text(
                "Refresh",
                style = MaterialTheme.typography.labelLarge,
                color = SurfPalette.Teal,
                modifier = Modifier.clickable(onClick = onRefreshPings),
            )
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search country, city or IP", color = SurfPalette.Muted) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfPalette.Teal.copy(alpha = 0.12f))
                        .clickable(onClick = onSmartConnect)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("\u26A1", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.size(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Smart Connect", style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (smartConnectEnabled) {
                                "Measures every endpoint and picks the fastest that answers"
                            } else {
                                "Enable in Settings to rank endpoints automatically"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = SurfPalette.Muted,
                        )
                    }
                }
            }

            items(filtered, key = { it.id }) { location ->
                ServerRow(
                    location = location,
                    rttMs = pings[location.endpoint],
                    selected = location.id == selectedId,
                    onClick = { onSelect(location) },
                )
            }

            if (filtered.isEmpty()) {
                item {
                    Text(
                        "No server matches \"$query\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SurfPalette.Muted,
                        modifier = Modifier.padding(24.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ServerRow(
    location: SurfLocation,
    rttMs: Int?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected) SurfPalette.Teal.copy(alpha = 0.10f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(location.emojiFlag, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(location.displayName, style = MaterialTheme.typography.titleMedium)
                if (location.serverSupportsAwg) {
                    Spacer(Modifier.size(6.dp))
                    Text(
                        "AWG",
                        style = MaterialTheme.typography.bodySmall,
                        color = SurfPalette.Mint,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(SurfPalette.Mint.copy(alpha = 0.14f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
            Text(
                "${location.ip}:${location.port}",
                style = MaterialTheme.typography.bodySmall,
                color = SurfPalette.Muted,
            )
        }
        PingBadge(rttMs)
    }
}
