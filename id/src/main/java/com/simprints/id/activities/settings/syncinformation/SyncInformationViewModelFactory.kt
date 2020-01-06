package com.simprints.id.activities.settings.syncinformation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.prefs.PreferencesManager

class SyncInformationViewModelFactory(private val personRepository: PersonRepository,
                                      private val personLocalDataSource: PersonLocalDataSource,
                                      private val preferencesManager: PreferencesManager,
                                      private val projectId: String,
                                      private val downSyncScopeRepository: PeopleDownSyncScopeRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(SyncInformationViewModel::class.java)) {
            SyncInformationViewModel(personRepository, personLocalDataSource, preferencesManager,
                projectId, downSyncScopeRepository) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }

}
