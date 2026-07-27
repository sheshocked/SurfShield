package com.surfshield

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.surfshield.data.LocationRepository
import com.surfshield.data.SurfLocation
import com.surfshield.ui.ConnectionStatus
import com.surfshield.ui.ProtocolType
import com.surfshield.ui.SurfColors
import com.surfshield.ui.components.ConnectionButton
import com.surfshield.ui.components.LocationSheet
import com.surfshield.ui.components.ProtocolSheet

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val locationsJson = assets.open("locations.json").bufferedReader().use { it.readText() }
        val repository = LocationRepository(locationsJson)
        val locations = repository.getLocations()

        setContent {
            SurfShieldTheme {
                SurfShieldApp(
                    locations = locations,
                    repository = repository,
                    onConnect = { location, protocol ->
                        startVpnService(location, protocol)
                    },
                    onDisconnect = {
                        stopVpnService()
                    }
                )
            }
        }
    }

    private fun startVpnService(location: SurfLocation, protocol: ProtocolType) {
        val intent = Intent(this, SurfsharkVpnService::class.java).apply {
            action = SurfsharkVpnService.ACTION_CONNECT
            putExtra(SurfsharkVpnService.EXTRA_CONFIG, location.id)
        }
        startForegroundService(intent)
    }

    private fun stopVpnService() {
        val intent = Intent(this, SurfsharkVpnService::class.java).apply {
            action = SurfsharkVpnService.ACTION_DISCONNECT
        }
        startService(intent)
    }
}

@Composable
fun SurfShieldTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = SurfColors.Primary,
            onPrimary = SurfColors.OnPrimary,
            secondary = SurfColors.Secondary,
            background = SurfColors.Background,
            surface = SurfColors.Surface,
            onBackground = SurfColors.OnBackground,
            onSurface = SurfColors.OnSurface,
            error = SurfColors.Error,
            outline = SurfColors.Outline
        ),
        content = content
    )
}

@Composable
fun SurfShieldApp(
    locations: List<SurfLocation>,
    repository: LocationRepository,
    onConnect: (SurfLocation, ProtocolType) -> Unit,
    onDisconnect: () -> Unit
) {
    val context = LocalContext.current
    var connectionStatus by remember { mutableStateOf(ConnectionStatus.DISCONNECTED) }
    var selectedLocation by remember { mutableStateOf(locations.find { it.id == "nl-ams" } ?: locations.firstOrNull()) }
    var selectedProtocol by remember { mutableStateOf(ProtocolType.AMNEZIAWG) }
    var showLocationSheet by remember { mutableStateOf(false) }
    var showProtocolSheet by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var connectionTime by remember { mutableStateOf(0L) }

    // Listen for VPN status broadcasts
    val statusReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.getStringExtra("status")) {
                    "connected" -> {
                        connectionStatus = ConnectionStatus.CONNECTED
                        connectionTime = System.currentTimeMillis()
                    }
                    "disconnected" -> {
                        connectionStatus = ConnectionStatus.DISCONNECTED
                        errorMessage = null
                    }
                    "error" -> {
                        connectionStatus = ConnectionStatus.ERROR
                        errorMessage = intent.getStringExtra("message")
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        context.registerReceiver(
            statusReceiver,
            IntentFilter(SurfsharkVpnService.ACTION_STATUS)
        )
        onDispose { context.unregisterReceiver(statusReceiver) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SurfColors.Background,
                        SurfColors.Surface,
                        SurfColors.Background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ====== TOP BAR ======
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo / Brand
                Text(
                    "SURFSHIELD",
                    color = SurfColors.OnBackground,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )

                // Settings icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfColors.SurfaceVariant)
                        .clickable { showSettings = !showSettings },
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚙️", fontSize = 18.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            // ====== LOCATION SELECTOR ======
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfColors.SurfaceVariant.copy(alpha = 0.5f))
                    .clickable { showLocationSheet = true }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    selectedLocation?.emojiFlag ?: "🌍",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 10.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        selectedLocation?.let { "${it.country} • ${it.city}" } ?: "Select Location",
                        color = SurfColors.OnBackground,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        selectedLocation?.let { "${it.id.uppercase()}  ⚡ ${it.resolvedIp}" } ?: "Tap to choose",
                        color = SurfColors.OnSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
                Text("▼", color = SurfColors.Muted, fontSize = 12.sp)
            }

            Spacer(Modifier.height(16.dp))

            // ====== PROTOCOL SELECTOR ======
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfColors.SurfaceVariant.copy(alpha = 0.3f))
                    .clickable { showProtocolSheet = true }
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val protocolIcon = when (selectedProtocol) {
                    ProtocolType.WIREGUARD -> "🔵"
                    ProtocolType.AMNEZIAWG -> "🛡️"
                    ProtocolType.SHADOWSOCKS -> "🚀"
                }
                Text(protocolIcon, fontSize = 16.sp, modifier = Modifier.padding(end = 8.dp))
                Text(
                    selectedProtocol.displayName,
                    color = SurfColors.OnBackground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    selectedProtocol.shortName,
                    color = SurfColors.Primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.weight(0.3f))

            // ====== CONNECTION BUTTON ======
            ConnectionButton(
                status = connectionStatus,
                size = 260.dp,
                onClick = {
                    when (connectionStatus) {
                        ConnectionStatus.DISCONNECTED,
                        ConnectionStatus.ERROR -> {
                            val loc = selectedLocation
                            if (loc != null) {
                                connectionStatus = ConnectionStatus.CONNECTING
                                errorMessage = null
                                onConnect(loc, selectedProtocol)
                            }
                        }
                        ConnectionStatus.CONNECTED,
                        ConnectionStatus.CONNECTING -> {
                            connectionStatus = ConnectionStatus.DISCONNECTED
                            onDisconnect()
                        }
                    }
                }
            )

            Spacer(Modifier.weight(0.2f))

            // ====== STATUS TEXT ======
            Text(
                text = when (connectionStatus) {
                    ConnectionStatus.CONNECTED -> "● Connected"
                    ConnectionStatus.CONNECTING -> "◌ Connecting..."
                    ConnectionStatus.DISCONNECTED -> "○ Tap to connect"
                    ConnectionStatus.ERROR -> "✕ Connection failed"
                },
                color = when (connectionStatus) {
                    ConnectionStatus.CONNECTED -> SurfColors.StatusConnected
                    ConnectionStatus.CONNECTING -> SurfColors.StatusConnecting
                    ConnectionStatus.ERROR -> SurfColors.StatusError
                    ConnectionStatus.DISCONNECTED -> SurfColors.OnSurfaceVariant
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            // Error message
            if (errorMessage != null) {
                Text(
                    errorMessage!!,
                    color = SurfColors.StatusError,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Connected info
            if (connectionStatus == ConnectionStatus.CONNECTED && selectedLocation != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "${selectedLocation!!.emojiFlag} ${selectedLocation!!.country} • ${selectedProtocol.shortName}",
                    color = SurfColors.OnSurfaceVariant,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(24.dp))

            // ====== QUICK ACTIONS ======
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton("🔄", "Scan") {
                    // Start server latency scan
                    startServerScan(context, locations, repository, selectedProtocol) { bestLocation ->
                        selectedLocation = bestLocation
                    }
                }
                QuickActionButton("📋", "Import") { /* Config import */ }
                QuickActionButton("📊", "Stats") { /* Show stats */ }
                QuickActionButton("ℹ️", "About") { /* About dialog */ }
            }
        }

        // ====== BOTTOM SHEETS ======
        if (showLocationSheet) {
            ModalBottomSheet(
                onDismissRequest = { showLocationSheet = false },
                containerColor = Color.Transparent,
                sheetMaxWidth = 500.dp
            ) {
                LocationSheet(
                    locations = locations,
                    selectedLocation = selectedLocation,
                    selectedProtocol = selectedProtocol,
                    onSelect = { loc ->
                        selectedLocation = loc
                        showLocationSheet = false
                    },
                    onDismiss = { showLocationSheet = false }
                )
            }
        }

        if (showProtocolSheet) {
            ModalBottomSheet(
                onDismissRequest = { showProtocolSheet = false },
                containerColor = Color.Transparent,
                sheetMaxWidth = 500.dp
            ) {
                ProtocolSheet(
                    selectedProtocol = selectedProtocol,
                    onSelect = { proto ->
                        selectedProtocol = proto
                        showProtocolSheet = false
                    },
                    onDismiss = { showProtocolSheet = false }
                )
            }
        }

        // ====== SETTINGS PANEL ======
        AnimatedVisibility(
            visible = showSettings,
            enter = slideInVertically { -it },
            exit = slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            SettingsPanel(
                onDismiss = { showSettings = false }
            )
        }
    }
}

@Composable
fun QuickActionButton(
    emoji: String,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(SurfColors.SurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 20.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            color = SurfColors.OnSurfaceVariant,
            fontSize = 11.sp
        )
    }
}

@Composable
fun SettingsPanel(
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfColors.Surface, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "⚙️ Settings",
                color = SurfColors.OnBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onDismiss) {
                Text("Done", color = SurfColors.Primary)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Settings options
        listOf(
            "🚀 Auto-connect" to "Connect on app launch",
            "🔒 Kill Switch" to "Block traffic if VPN drops",
            "🌐 Split Tunnel" to "Exclude apps from VPN",
            "📡 DNS" to "Custom DNS servers",
            "📦 MTU" to "Packet size optimization"
        ).forEach { (title, desc) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfColors.SurfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = SurfColors.OnBackground, fontSize = 14.sp)
                    Text(desc, color = SurfColors.OnSurfaceVariant, fontSize = 11.sp)
                }
                Text(">", color = SurfColors.Muted)
            }
        }
    }
}

private fun startServerScan(
    context: android.content.Context,
    locations: List<SurfLocation>,
    repository: LocationRepository,
    protocol: ProtocolType,
    onBestFound: (SurfLocation) -> Unit
) {
    android.widget.Toast.makeText(context, "🔍 Scanning servers...", android.widget.Toast.LENGTH_SHORT).show()

    kotlinx.coroutines.MainScope().launch {
        val results = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val executor = java.util.concurrent.Executors.newFixedThreadPool(10)
            val results = java.util.Collections.synchronizedList(mutableListOf<Pair<SurfLocation, Long>>())
            val latch = java.util.concurrent.CountDownLatch(locations.size)

            locations.forEach { loc ->
                executor.execute {
                    val start = System.currentTimeMillis()
                    try {
                        val socket = java.net.Socket()
                        socket.connect(java.net.InetSocketAddress(loc.resolvedIp, loc.port), 1000)
                        socket.close()
                        val latency = System.currentTimeMillis() - start
                        results.add(loc to latency)
                    } catch (_: Exception) { }
                    latch.countDown()
                }
            }

            latch.await(15, java.util.concurrent.TimeUnit.SECONDS)
            executor.shutdown()
            results.sortedBy { it.second }
        }

        if (results.isNotEmpty()) {
            val best = results.first().first
            onBestFound(best)
            android.widget.Toast.makeText(
                context,
                "✅ Best: ${best.emojiFlag} ${best.country} (${results.first().second}ms)",
                android.widget.Toast.LENGTH_LONG
            ).show()
        } else {
            android.widget.Toast.makeText(context, "❌ No servers found", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
