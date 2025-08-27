package com.simprints.infra.events.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.tools.time.TimeHelper
import dagger.hilt.InstallIn
import dagger.hilt.android.EarlyEntryPoint
import dagger.hilt.android.EarlyEntryPoints
import dagger.hilt.components.SingletonComponent

@ExcludedFromGeneratedTestCoverageReports("Platform glue code, actual logic is in the use cases")
internal class SessionTerminationReceiver : BroadcastReceiver() {
    // Normal Hilt injection works fine when running the app normally.
    // However, during Android instrumentation tests the process crashes because
    // the BroadcastReceiver is created before Hilt can inject dependencies.
    // To work around this, we use an EarlyEntryPoint to fetch dependencies manually.
    // See: https://github.com/google/dagger/issues/4903
    @EarlyEntryPoint
    @InstallIn(SingletonComponent::class)
    interface SessionTerminationEntryPoint {
        fun timeHelper(): TimeHelper

        fun closeSessionUseCase(): CloseSessionIfPresentUseCase
    }

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        // Get the early entry point to access dependencies
        val entryPoint = EarlyEntryPoints.get(
            context.applicationContext,
            SessionTerminationEntryPoint::class.java,
        )
        val timeHelper = entryPoint.timeHelper()
        val closeSessionUseCase = entryPoint.closeSessionUseCase()

        timeHelper.ensureTrustworthiness()
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            closeSessionUseCase()
        }
    }
}
