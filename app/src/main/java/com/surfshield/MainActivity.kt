package com.surfshield

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.surfshield.data.SurfLocation
import com.surfshield.ui.ConnectionStatus
import com.surfshield.ui.ProtocolType
import com.surfshield.ui.SurfColors
import com.surfshield.ui.components.ConnectionButton
import com.surfshield.ui.components.LocationSheet
import com.surfshield.ui.components.ProtocolSheet
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {

    companion object {
        const val PREFS_NAME = "surfshield_prefs"
        const val KEY_LOCATION_ID = "selected_location_id"
        const val KEY_PROTOCOL = "selected_protocol"
    }

    private lateinit var prefs: SharedPreferences
    private lateinit var connectionReceiver: BroadcastReceiver
    private var isConnected by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        connectionReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    "com.surfshield.CONNECTED" -> isConnected = true
                    "com.surfshield.DISCONNECTED" -> isConnected = false
                    "com.surfshield.STATUS_UPDATE" -> {
                        // Update UI with status data
                    }
                }
            }
        }

        registerReceiver(connectionReceiver, IntentFilter().apply {
            addAction("com.surfshield.CONNECTED")
            addAction("com.surfshield.DISCONNECTED")
            addAction("com.surfshield.STATUS_UPDATE")
        }, RECEIVER_NOT_EXPORTED)

        setContent {
            SurfShieldTheme {
                MainScreen(
                    prefs = prefs,
                    isConnected = isConnected,
                    onToggleConnection = ::toggleConnection
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(connectionReceiver)
    }

    private fun toggleConnection() {
        if (isConnected) {
            stopService(Intent(this, SurfsharkVpnService::class.java))
        } else {
            val intent = Intent(this, SurfsharkVpnService::class.java)
            startForegroundService(intent)
        }
    }
}

@Composable
fun SurfShieldTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = SurfColors.Primary,
            secondary = SurfColors.PrimaryDim,
            background = SurfColors.Background,
            surface = SurfColors.Surface,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = SurfColors.OnBackground,
            onSurface = SurfColors.OnBackground,
            surfaceVariant = SurfColors.SurfaceVariant,
            onSurfaceVariant = SurfColors.OnSurfaceVariant,
            outline = SurfColors.Muted
        ),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    prefs: SharedPreferences,
    isConnected: Boolean,
    onToggleConnection: () -> Unit
) {
    val context = LocalContext.current
    var locations by remember { mutableStateOf<List<SurfLocation>>(emptyList()) }
    var selectedLocation by remember { mutableStateOf<SurfLocation?>(null) }
    var selectedProtocol by remember { mutableStateOf(ProtocolType.WIREGUARD) }
    var status by remember { mutableStateOf(ConnectionStatus.DISCONNECTED) }
    var showLocationSheet by remember { mutableStateOf(false) }
    var showProtocolSheet by remember { mutableStateOf(false) }
    var dataUsed by remember { mutableStateOf("0 MB") }
    var connectionTime by remember { mutableStateOf("--:--") }

    // Load locations
    LaunchedEffect(Unit) {
        locations = loadLocationsFromJson(context)
        val savedId = prefs.getString(KEY_LOCATION_ID, null)
        if (savedId != null) {
            selectedLocation = locations.find { it.id == savedId }
        }
        if (selectedLocation == null && locations.isNotEmpty()) {
            selectedLocation = locations.first()
        }
        val savedProtocol = prefs.getString(KEY_PROTOCOL, null)
        if (savedProtocol != null) {
            selectedProtocol = ProtocolType.valueOf(savedProtocol)
        }
    }

    // Update status based on connection
    LaunchedEffect(isConnected) {
        status = when {
            isConnected -> ConnectionStatus.CONNECTED
            else -> ConnectionStatus.DISCONNECTED
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SurfColors.Background, SurfColors.Surface, SurfColors.Background)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            // Logo area
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(SurfColors.PrimaryDim, SurfColors.Primary.copy(alpha = 0.3f))
                            ),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚡", fontSize = 28.sp)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "SurfShield",
                    color = SurfColors.OnBackground,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Premium Surfshark VPN",
                    color = SurfColors.OnSurfaceVariant,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.weight(0.15f))

            // Selected location info
            if (selectedLocation != null) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "${selectedLocation!!.emojiFlag} ${selectedLocation!!.country}",
                        color = SurfColors.OnBackground,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        selectedLocation!!.id.uppercase(),
                        color = SurfColors.OnSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.weight(0.1f))

            // Connection button
            ConnectionButton(
                status = status,
                size = 250.dp,
                onClick = onToggleConnection
            )

            Spacer(Modifier.weight(0.1f))

            // Connection info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfColors.SurfaceElevated),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InfoItem("Data", dataUsed)
                    InfoItem("Time", connectionTime)
                    InfoItem("Protocol", selectedProtocol.name)
                }
            }

            Spacer(Modifier.weight(0.15f))

            // Bottom controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showLocationSheet = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SurfColors.Primary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("🌍 Location", fontSize = 14.sp)
                }
                OutlinedButton(
                    onClick = { showProtocolSheet = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SurfColors.Primary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        when (selectedProtocol) {
                            ProtocolType.WIREGUARD -> "⚡ WG"
                            ProtocolType.AMNEZIAWG -> "🛡️ AWG"
                            ProtocolType.SHADOWSOCKS -> "🌊 SS"
                            ProtocolType.VLESS -> "🔒 VL"
                        },
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    // Location bottom sheet
    if (showLocationSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLocationSheet = false },
            containerColor = SurfColors.Surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            LocationSheet(
                locations = locations,
                selectedLocation = selectedLocation,
                selectedProtocol = selectedProtocol,
                onSelect = { loc ->
                    selectedLocation = loc
                    prefs.edit().putString(KEY_LOCATION_ID, loc.id).apply()
                    showLocationSheet = false
                },
                onDismiss = { showLocationSheet = false }
            )
        }
    }

    // Protocol bottom sheet
    if (showProtocolSheet) {
        ModalBottomSheet(
            onDismissRequest = { showProtocolSheet = false },
            containerColor = SurfColors.Surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            ProtocolSheet(
                selectedProtocol = selectedProtocol,
                onSelect = { proto ->
                    selectedProtocol = proto
                    prefs.edit().putString(KEY_PROTOCOL, proto.name).apply()
                    showProtocolSheet = false
                },
                onDismiss = { showProtocolSheet = false }
            )
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = SurfColors.OnBackground, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Text(label, color = SurfColors.OnSurfaceVariant, fontSize = 11.sp)
    }
}

private fun loadLocationsFromJson(context: Context): List<SurfLocation> {
    return try {
        val json = context.assets.open("locations.json").bufferedReader().use { it.readText() }
        val arr = JSONObject(json).getJSONArray("locations")
        (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            SurfLocation(
                id = obj.getString("id"),
                country = obj.getString("country"),
                emojiFlag = obj.getString("emojiFlag"),
                domain = obj.getString("domain"),
                resolvedIp = obj.optString("resolvedIp", ""),
                publicKey = obj.getString("publicKey"),
                privateKey = obj.getString("privateKey"),
                address = obj.optString("address", "10.14.0.2/16")
            )
        }
    } catch (e: Exception) {
        emptyList()
    }
}

// BootReceiver for auto-connect
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
            if (prefs.getBoolean("auto_connect", false)) {
                val vpnIntent = Intent(context, SurfsharkVpnService::class.java)
                context.startForegroundService(vpnIntent)
            }
        }
    }
}
