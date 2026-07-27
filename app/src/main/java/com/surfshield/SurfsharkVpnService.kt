package com.surfshield

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat

class SurfsharkVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "surfshield_vpn"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startVpn()
        return START_STICKY
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startVpn() {
        val builder = Builder()
        builder.setSession("SurfShield")
        builder.addAddress("10.14.0.2", 32)
        builder.addRoute("0.0.0.0", 0)
        builder.addDnsServer("162.252.172.57")
        builder.addDnsServer("149.154.159.92")
        builder.setMtu(1500)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(false)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setBlocking(true)
        }

        try {
            vpnInterface = builder.establish()
            sendBroadcast(Intent("com.surfshield.CONNECTED"))
        } catch (e: Exception) {
            sendBroadcast(Intent("com.surfshield.DISCONNECTED"))
        }
    }

    private fun stopVpn() {
        vpnInterface?.close()
        vpnInterface = null
        sendBroadcast(Intent("com.surfshield.DISCONNECTED"))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SurfShield VPN",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "SurfShield VPN connection status"
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SurfShield VPN")
            .setContentText("Connected - Securing your connection")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}

