package com.surfshield

import android.content.Context
import android.content.SharedPreferences
import android.net.VpnService
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.*
import java.io.File
import java.net.*

class SurfsharkVpnService : VpnService() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false

    private inner class VpnConnection(
        val configId: String,
        val protocol: String,  // "wireguard", "amneziawg", "shadowsocks"
        val serverIp: String,
        val serverPort: Int,
        val privateKey: String,
        val publicKey: String,
        val address: String,
        val dns: String,
        val jc: Int = 3, val jd: Int = 5,
        val jmin: Int = 40, val jmax: Int = 70,
        val s1: Int = 0, val s2: Int = 0,
        val h1: Int = 1, val h2: Int = 2,
        val h3: Int = 3, val h4: Int = 4
    )

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                val config = intent.getSerializableExtra(EXTRA_CONFIG) as? VpnConnection ?: return START_NOT_STICKY
                startVpn(config)
            }
            ACTION_DISCONNECT -> stopVpn()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopVpn()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun startVpn(config: VpnConnection) {
        if (isRunning) return
        isRunning = true

        serviceScope.launch {
            try {
                // Build VPN interface
                val builder = Builder()
                builder.setSession("SurfShield")
                builder.setMtu(1300)

                // Parse address
                val addr = config.address.split('/')
                if (addr.isNotEmpty()) {
                    builder.addAddress(addr[0], if (addr.size > 1) addr[1].toInt() else 32)
                }

                // Add DNS
                config.dns.split(",").forEach { dns ->
                    try {
                        builder.addDnsServer(InetAddress.getByName(dns.trim()))
                    } catch (e: Exception) { }
                }

                // Add routes
                builder.addRoute("0.0.0.0", 0)

                // Set DNS bypass
                builder.setBlocking(true)

                // Establish interface
                vpnInterface = builder.establish()
                if (vpnInterface == null) {
                    sendBroadcast(android.content.Intent(ACTION_STATUS).apply {
                        putExtra("status", "error")
                        putExtra("message", "Failed to create VPN interface")
                    })
                    return@launch
                }

                sendBroadcast(android.content.Intent(ACTION_STATUS).apply {
                    putExtra("status", "connected")
                    putExtra("config_id", config.configId)
                    putExtra("protocol", config.protocol)
                })

                // Keep alive - read from interface
                val inputStream = java.io.FileInputStream(vpnInterface!!.fileDescriptor)
                val outputStream = java.io.FileOutputStream(vpnInterface!!.fileDescriptor)
                val buffer = ByteArray(32767)

                while (isRunning) {
                    try {
                        val read = inputStream.read(buffer)
                        if (read > 0) {
                            outputStream.write(buffer, 0, read)
                            outputStream.flush()
                        }
                    } catch (e: Exception) {
                        break
                    }
                }

            } catch (e: Exception) {
                sendBroadcast(android.content.Intent(ACTION_STATUS).apply {
                    putExtra("status", "error")
                    putExtra("message", e.message ?: "Unknown error")
                })
            } finally {
                stopVpn()
            }
        }
    }

    private fun stopVpn() {
        isRunning = false
        try {
            vpnInterface?.close()
        } catch (_: Exception) { }
        vpnInterface = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()

        sendBroadcast(android.content.Intent(ACTION_STATUS).apply {
            putExtra("status", "disconnected")
        })
    }

    companion object {
        const val ACTION_CONNECT = "com.surfshield.CONNECT"
        const val ACTION_DISCONNECT = "com.surfshield.DISCONNECT"
        const val ACTION_STATUS = "com.surfshield.STATUS"
        const val EXTRA_CONFIG = "config"
    }
}
