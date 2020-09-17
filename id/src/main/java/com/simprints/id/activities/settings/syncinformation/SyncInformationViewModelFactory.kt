package com.simprints.id.activities.settings.syncinformation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.data.db.subjects_sync.down.SubjectsDownSyncScopeRepository
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import com.simprints.id.data.images.repository.ImageRepository
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.subjects.master.SubjectsSyncManager

class SyncInformationViewModelFactory(private val personRepository: SubjectRepository,
                                      private val subjectLocalDataSource: SubjectLocalDataSource,
                                      private val preferencesManager: PreferencesManager,
                                      private val projectId: String,
                                      private val downSyncScopeRepository: SubjectsDownSyncScopeRepository,
                                      private val imageRepository: ImageRepository,
                                      private val subjectsSyncManager: SubjectsSyncManager) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(SyncInformationViewModel::class.java)) {
            SyncInformationViewModel(personRepository, subjectLocalDataSource, preferencesManager,
                projectId, downSyncScopeRepository, imageRepository, subjectsSyncManager) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }

}
