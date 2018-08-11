package com.simprints.id.data.prefs.sync.people

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.tools.delegates.PrimitivePreference


class SyncPeoplePreferencesManagerImpl(prefs: ImprovedSharedPreferences) : SyncPeoplePreferencesManager {

    companion object {
        private const val SCHEDULED_SYNC_PEOPLE_WORK_REQUEST_ID: String = "ScheduledPeopleSyncWorkRequest"
        private const val SCHEDULED_SYNC_PEOPLE_WORK_REQUEST_DEFAULT: String = ""
    }

    override var scheduledPeopleSyncWorkRequestId: String
        by PrimitivePreference(prefs, SCHEDULED_SYNC_PEOPLE_WORK_REQUEST_ID, SCHEDULED_SYNC_PEOPLE_WORK_REQUEST_DEFAULT)
}
