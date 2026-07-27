package com.surfshield.ui.components

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
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(SurfColors.Muted)
                .align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "🌍 Select Location",
            color = SurfColors.OnBackground,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Text(
            "${locations.size} servers available",
            color = SurfColors.OnSurfaceVariant,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 24.dp, top = 4.dp, end = 24.dp, bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(locations, key = { it.id }) { location ->
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
        Text(location.emojiFlag, fontSize = 28.sp, modifier = Modifier.padding(end = 14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${location.country} • ${location.city}",
                color = if (isSelected) SurfColors.Primary else SurfColors.OnBackground,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                location.id.uppercase(),
                color = SurfColors.OnSurfaceVariant,
                fontSize = 12.sp
            )
        }
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
