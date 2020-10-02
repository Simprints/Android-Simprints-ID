package com.simprints.id.activities.settings.syncinformation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.data.db.events_sync.down.EventDownSyncScopeRepository
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.images.repository.ImageRepository
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.id.services.sync.events.up.EventUpSyncHelper

class SyncInformationViewModelFactory(private val downySyncHelper: EventDownSyncHelper,
                                      private val upSyncHelper: EventUpSyncHelper,
                                      private val subjectRepository: SubjectRepository,
                                      private val preferencesManager: PreferencesManager,
                                      private val projectId: String,
                                      private val eventDownSyncScopeRepository: EventDownSyncScopeRepository,
                                      private val imageRepository: ImageRepository,
                                      private val eventSyncManager: EventSyncManager) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(SyncInformationViewModel::class.java)) {
            SyncInformationViewModel(downySyncHelper, upSyncHelper, subjectRepository, preferencesManager,
                projectId, eventDownSyncScopeRepository, imageRepository, eventSyncManager) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }

}
