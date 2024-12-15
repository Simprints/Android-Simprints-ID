package com.simprints.core.tools.utils

import android.content.Context
import android.os.PowerManager
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports

@ExcludedFromGeneratedTestCoverageReports("Platform glue code")
object BatteryOptimizationUtils {
    fun isFollowingBatteryOptimizations(context: Context): Boolean = (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
        .isIgnoringBatteryOptimizations(context.packageName)
        .not()
}
