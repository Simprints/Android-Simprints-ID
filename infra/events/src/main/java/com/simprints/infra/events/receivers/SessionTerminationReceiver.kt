package com.simprints.infra.events.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@ExcludedFromGeneratedTestCoverageReports("Platform glue code, actual logic is in the use cases")
@AndroidEntryPoint
internal class SessionTerminationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var closeSessionUseCase: CloseSessionIfPresentUseCase

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            closeSessionUseCase()
        }
    }
}
