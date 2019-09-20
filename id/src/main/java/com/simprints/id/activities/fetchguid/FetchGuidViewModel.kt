package com.simprints.id.activities.fetchguid

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.id.data.db.PersonFetchResult.PersonSource
import com.simprints.id.data.db.person.PersonRepository
import kotlinx.coroutines.launch

class FetchGuidViewModel(private val personRepository: PersonRepository) : ViewModel() {

    var personFetch = MutableLiveData<PersonSource>()

    fun fetchGuid(projectId: String, verifyGuid: String) {
        viewModelScope.launch {
            val personFetchResult = personRepository.loadFromRemoteIfNeeded(projectId, verifyGuid)
            personFetch.postValue(personFetchResult.personSource)
        }
    }
}
