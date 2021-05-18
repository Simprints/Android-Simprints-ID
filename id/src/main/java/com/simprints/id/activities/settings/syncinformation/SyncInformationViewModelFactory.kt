package com.simprints.id.activities.settings.syncinformation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.eventsystem.subject.SubjectRepository
import com.simprints.id.data.images.repository.ImageRepository
import com.simprints.core.sharedpreferences.PreferencesManager
import com.simprints.id.services.sync.events.down.EventDownSyncHelper

class SyncInformationViewModelFactory(private val downySyncHelper: EventDownSyncHelper,
                                      private val eventRepository: com.simprints.eventsystem.event.EventRepository,
                                      private val subjectRepository: SubjectRepository,
                                      private val preferencesManager: PreferencesManager,
                                      private val projectId: String,
                                      private val eventDownSyncScopeRepository: com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepository,
                                      private val imageRepository: ImageRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(SyncInformationViewModel::class.java)) {
            SyncInformationViewModel(
                downySyncHelper,
                eventRepository,
                subjectRepository,
                preferencesManager,
                projectId,
                eventDownSyncScopeRepository,
                imageRepository
            ) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }

}
