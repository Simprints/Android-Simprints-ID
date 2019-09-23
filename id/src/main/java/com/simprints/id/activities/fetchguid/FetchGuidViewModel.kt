package com.simprints.id.activities.fetchguid

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.id.data.db.PersonFetchResult
import com.simprints.id.data.db.PersonFetchResult.PersonSource
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.tools.utils.SimNetworkUtils
import kotlinx.coroutines.runBlocking
import org.jetbrains.anko.doAsync

class FetchGuidViewModel(private val personRepository: PersonRepository,
                         private val simNetworkUtils: SimNetworkUtils) : ViewModel() {

    var personFetch = MutableLiveData<PersonSource>()

    fun fetchGuid(projectId: String, verifyGuid: String) {
        doAsync {
            val personFetchResult = runBlocking {
                try {
                    personRepository.loadFromRemoteIfNeeded(projectId, verifyGuid)
                } catch (t: Throwable) {
                    getPersonFetchResultForError()
                }
            }
            personFetch.postValue(personFetchResult.personSource)
        }
    }

    private fun getPersonFetchResultForError() =
        if (simNetworkUtils.isConnected()) {
            PersonFetchResult(null, PersonSource.NOT_FOUND_IN_LOCAL_AND_REMOTE)
        } else {
            PersonFetchResult(null, PersonSource.NOT_FOUND_IN_LOCAL_REMOTE_CONNECTION_ERROR)
        }
}
