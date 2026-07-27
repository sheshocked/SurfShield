package com.surfshield.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppEntry(
    val packageName: String,
    val label: String,
    val icon: Drawable?,
    val isSystem: Boolean,
)

/**
 * Supplies the app list for split tunnelling.
 *
 * Only launchable packages are listed. Enumerating every installed package
 * would flood the picker with hundreds of system services the user can neither
 * recognise nor meaningfully route.
 */
object InstalledApps {

    suspend fun load(context: Context, includeSystem: Boolean = false): List<AppEntry> =
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

            @Suppress("DEPRECATION")
            val resolved = pm.queryIntentActivities(launcherIntent, 0)

            resolved.asSequence()
                .mapNotNull { it.activityInfo?.applicationInfo }
                .distinctBy { it.packageName }
                .filter { it.packageName != context.packageName }
                .map { info ->
                    AppEntry(
                        packageName = info.packageName,
                        label = runCatching { pm.getApplicationLabel(info).toString() }
                            .getOrDefault(info.packageName),
                        icon = runCatching { pm.getApplicationIcon(info) }.getOrNull(),
                        isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    )
                }
                .filter { includeSystem || !it.isSystem }
                .sortedBy { it.label.lowercase() }
                .toList()
        }
}
