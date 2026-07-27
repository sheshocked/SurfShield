package com.surfshield.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.surfshield.data.SurfLocation
import com.surfshield.ui.ProtocolType
import com.surfshield.ui.SurfColors

@Composable
fun LocationSheet(
    locations: List<SurfLocation>,
    selectedLocation: SurfLocation?,
    selectedProtocol: ProtocolType,
    onSelect: (SurfLocation) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfColors.Surface, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .padding(top = 16.dp)
    ) {
        // Drag handle
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(SurfColors.Muted)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(16.dp))

        // Title
        Text(
            "🌍 Select Location",
            color = SurfColors.OnBackground,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Text(
            "${locations.size} servers available • ${selectedProtocol.displayName}",
            color = SurfColors.OnSurfaceVariant,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 24.dp, top = 4.dp, end = 24.dp, bottom = 16.dp)
        )

        // Search bar
        var searchQuery by remember { mutableStateOf("") }
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search country or city...", color = SurfColors.Muted) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfColors.SurfaceVariant),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SurfColors.SurfaceVariant,
                unfocusedContainerColor = SurfColors.SurfaceVariant,
                focusedTextColor = SurfColors.OnBackground,
                unfocusedTextColor = SurfColors.OnBackground,
                cursorColor = SurfColors.Primary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        // Location list
        val filtered = if (searchQuery.isBlank()) locations
        else locations.filter {
            it.country.contains(searchQuery, ignoreCase = true) ||
            it.city.contains(searchQuery, ignoreCase = true) ||
            it.id.contains(searchQuery, ignoreCase = true)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filtered, key = { it.id }) { location ->
                LocationItem(
                    location = location,
                    isSelected = location.id == selectedLocation?.id,
                    onClick = { onSelect(location) }
                )
            }
        }
    }
}

@Composable
fun LocationItem(
    location: SurfLocation,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) SurfColors.Primary.copy(alpha = 0.15f) else SurfColors.SurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Flag emoji
        Text(
            location.emojiFlag,
            fontSize = 28.sp,
            modifier = Modifier.padding(end = 14.dp)
        )

        // Country info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${location.country}  •  ${location.city}",
                color = if (isSelected) SurfColors.Primary else SurfColors.OnBackground,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${location.id.uppercase()}  ⚡ ${location.protocolLabel}",
                color = SurfColors.OnSurfaceVariant,
                fontSize = 12.sp,
                maxLines = 1
            )
        }

        // Selected indicator
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(SurfColors.Primary)
            )
        }
    }
}
