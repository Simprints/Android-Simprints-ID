package com.simprints.id.activities.settings.syncinformation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.data.db.events_sync.down.EventDownSyncScopeRepository
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.sync.events.down.EventDownSyncHelper

class SyncInformationViewModelFactory(private val downySyncHelper: EventDownSyncHelper,
                                      private val subjectLocalDataSource: SubjectLocalDataSource,
                                      private val preferencesManager: PreferencesManager,
                                      private val projectId: String,
                                      private val downSyncScopeRepository: EventDownSyncScopeRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(SyncInformationViewModel::class.java)) {
            SyncInformationViewModel(downySyncHelper, subjectLocalDataSource, preferencesManager,
                projectId, downSyncScopeRepository) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }

}
