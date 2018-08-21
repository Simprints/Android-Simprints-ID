package com.simprints.id.data.prefs.sync.sessions

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.preferenceType.PrimitivePreference


class SyncSessionsPreferencesManagerImpl(prefs: ImprovedSharedPreferences) : SyncSessionsPreferencesManager {

    companion object {
        private const val SCHEDULED_SYNC_SESSIONS_WORK_REQUEST_ID: String = "ScheduledSessionsSyncWorkRequest"
        private const val SCHEDULED_SYNC_SESSIONS_WORK_REQUEST_DEFAULT: String = ""
    }

    override var scheduledSessionsSyncWorkRequestId: String
        by PrimitivePreference(prefs, SCHEDULED_SYNC_SESSIONS_WORK_REQUEST_ID, SCHEDULED_SYNC_SESSIONS_WORK_REQUEST_DEFAULT)
}
