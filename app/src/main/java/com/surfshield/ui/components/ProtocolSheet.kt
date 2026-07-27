package com.surfshield.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
            .padding(top = 16.dp, start = 24.dp, end = 24.dp, bottom = 32.dp)
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(SurfColors.Muted)
                .align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(20.dp))
        Text("🔌 Protocol", color = SurfColors.OnBackground, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Choose the VPN protocol for your connection", color = SurfColors.OnSurfaceVariant, fontSize = 14.sp)
        Spacer(Modifier.height(20.dp))

        val protocols = listOf(
            ProtocolType.WIREGUARD to "⚡ WireGuard",
            ProtocolType.AMNEZIAWG to "🛡️ AmneziaWG (Obfuscated)",
            ProtocolType.SHADOWSOCKS to "🌊 Shadowsocks"
        )

        protocols.forEach { (type, label) ->
            ProtocolItem(
                label = label,
                description = when (type) {
                    ProtocolType.WIREGUARD -> "Fast & secure, but detectable"
                    ProtocolType.AMNEZIAWG -> "Obfuscated WG, bypasses DPI"
                    ProtocolType.SHADOWSOCKS -> "Legacy proxy protocol"
                    ProtocolType.VLESS -> "VLESS over TLS"
                },
                isSelected = type == selectedProtocol,
                onClick = { onSelect(type) }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ProtocolItem(
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) SurfColors.Primary.copy(alpha = 0.15f) else SurfColors.SurfaceVariant
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(label, color = SurfColors.OnBackground, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Text(description, color = SurfColors.OnSurfaceVariant, fontSize = 13.sp, modifier = Modifier.padding(top = 2.dp))
    }
}
