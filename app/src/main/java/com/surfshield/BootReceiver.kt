package com.surfshield

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.VpnService
import com.surfshield.data.AppSettings
import com.surfshield.data.LocationRepository
import com.surfshield.vpn.TunnelManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Reconnects after a reboot when the user opted in.
 *
 * VpnService.prepare() is checked first: consent granted in a previous session
 * survives reboots, but if it was revoked we must not attempt a silent connect,
 * because a background component cannot legally show the consent dialog.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) return

        val settings = AppSettings.get(context)
        if (!settings.autoConnectOnBoot) return
        if (VpnService.prepare(context) != null) return

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val locations = LocationRepository.load(context)
                val target = locations.firstOrNull { it.id == settings.lastLocationId }
                    ?: locations.firstOrNull()
                    ?: return@launch
                TunnelManager.get(context).connect(target, locations)
            } finally {
                pending.finish()
            }
        }
    }
}
