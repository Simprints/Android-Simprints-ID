package com.simprints.fingerprint.infra.scanner.tools

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class BatteryLevelChecker @Inject constructor(
    @ApplicationContext val context: Context,
) {
    fun isLowBattery(): Boolean {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }
        return if (batteryStatus != null) {
            getBatteryPercent(batteryStatus) <= LOW_BATTERY_LEVEL
        } else {
            false // If we can't determine the battery level, act like we are not on low battery
        }
    }

    private fun getBatteryPercent(batteryStatus: Intent): Float {
        val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        return level.toFloat() * 100f / scale.toFloat()
    }

    companion object {
        const val LOW_BATTERY_LEVEL = 15
    }
}
