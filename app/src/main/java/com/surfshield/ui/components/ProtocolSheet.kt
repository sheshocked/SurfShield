package com.surfshield.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.surfshield.ui.ProtocolType
import com.surfshield.ui.SurfColors

@Composable
fun ProtocolSheet(
    selectedProtocol: ProtocolType,
    onSelect: (ProtocolType) -> Unit,
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

        Spacer(Modifier.height(20.dp))

        Text(
            "🔒 Protocol",
            color = SurfColors.OnBackground,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Text(
            "Choose your VPN tunneling protocol",
            color = SurfColors.OnSurfaceVariant,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 24.dp, top = 4.dp, end = 24.dp, bottom = 20.dp)
        )

        // Protocol options
        ProtocolType.values().forEach { protocol ->
            val isSelected = protocol == selectedProtocol
            val bgColor = if (isSelected) SurfColors.Primary.copy(alpha = 0.15f)
                          else SurfColors.SurfaceVariant

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgColor)
                    .clickable { onSelect(protocol) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Protocol icon
                val icon = when (protocol) {
                    ProtocolType.WIREGUARD -> "🔵"
                    ProtocolType.AMNEZIAWG -> "🛡️"
                    ProtocolType.SHADOWSOCKS -> "🚀"
                }
                Text(icon, fontSize = 24.sp, modifier = Modifier.padding(end = 14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        protocol.displayName,
                        color = if (isSelected) SurfColors.Primary else SurfColors.OnBackground,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                    Text(
                        protocol.description,
                        color = SurfColors.OnSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(SurfColors.Primary)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}
