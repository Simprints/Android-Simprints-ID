package com.simprints.id.activities.fetchguid

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.id.data.db.PersonSource
import com.simprints.id.data.db.person.PersonRepository
import kotlinx.coroutines.runBlocking

class FetchGuidViewModel(private val personRepository: PersonRepository) : ViewModel() {

    var personFetch = MutableLiveData<PersonSource>()

    fun fetchGuid(projectId: String, verifyGuid: String) {
        val personFetchResult = runBlocking {
            personRepository.loadFromRemoteIfNeeded(projectId, verifyGuid)
        }

        personFetch.value = personFetchResult.personSource
    }
}
