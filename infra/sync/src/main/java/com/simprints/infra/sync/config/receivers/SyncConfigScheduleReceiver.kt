package com.simprints.infra.sync.config.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.simprints.infra.sync.SyncOrchestrator
import javax.inject.Inject

/**
 * Updating to a new version might enable features that require specific configuration
 * that has not been saved in older versions (e.g. tokenization keys).
 * Therefore it makes sense to refresh the configuration ASAP.
 */
class SyncConfigScheduleReceiver : BroadcastReceiver() {
    @Inject
    lateinit var syncOrchestrator: SyncOrchestrator

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (Intent.ACTION_MY_PACKAGE_REPLACED == intent.action) {
            syncOrchestrator.startConfigSync()
        }
    }
}
