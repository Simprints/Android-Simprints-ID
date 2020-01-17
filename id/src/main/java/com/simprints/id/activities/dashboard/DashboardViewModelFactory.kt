package com.simprints.id.activities.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager
import com.simprints.id.tools.device.DeviceManager

class DashboardViewModelFactory(private val peopleSyncManager: PeopleSyncManager,
                                private val deviceManager: DeviceManager,
                                private val preferencesManager: PreferencesManager,
                                private val peopleDownSyncScopeRepository: PeopleDownSyncScopeRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            DashboardViewModel(peopleSyncManager, deviceManager, preferencesManager, peopleDownSyncScopeRepository) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
