package com.simprints.infra.events.deviceinfo

import android.app.ActivityManager
import android.content.Context
import android.os.PowerManager
import android.os.StatFs
import com.simprints.infra.events.event.domain.models.scope.DeviceUsage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DeviceUsageInfoProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun getDeviceUsageInfo(): DeviceUsage = DeviceUsage(
        availableStorageMb = getAvailableStorageMb(),
        availableRamMb = getAvailableRamMb(),
        isBatterySaverOn = isBatterySaverOn(),
    )

    private fun getAvailableStorageMb(): Long? = runCatching {
        with(StatFs(context.filesDir.absolutePath)) {
            (availableBlocksLong * blockSizeLong) / BYTES_IN_MEGABYTE
        }
    }.getOrNull()

    private fun getAvailableRamMb(): Long? = runCatching {
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                ?: return@runCatching null
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        memoryInfo.availMem / BYTES_IN_MEGABYTE
    }.getOrNull()

    private fun isBatterySaverOn(): Boolean? = runCatching {
        val powerManager =
            context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        powerManager?.isPowerSaveMode
    }.getOrNull()

    private companion object {
        private const val BYTES_IN_MEGABYTE = 1024L * 1024L
    }
}
