package com.simprints.id.data.prefs.sync

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.tools.delegates.PrimitivePreference


class SyncPreferencesManagerImpl(prefs: ImprovedSharedPreferences) : SyncPreferencesManager {

    companion object {
        private const val SCHEDULED_SYNC_WORK_REQUEST_ID: String = "ScheduledSyncWorkRequest"
        private const val SCHEDULED_SYNC_WORK_REQUEST_DEFAULT: String = ""
    }

    override var scheduledSyncWorkRequestId: String
        by PrimitivePreference(prefs, SCHEDULED_SYNC_WORK_REQUEST_ID, SCHEDULED_SYNC_WORK_REQUEST_DEFAULT)
}
