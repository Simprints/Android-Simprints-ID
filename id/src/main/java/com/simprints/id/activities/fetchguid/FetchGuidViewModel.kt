package com.simprints.id.activities.fetchguid

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.CandidateReadEvent
import com.simprints.id.data.db.PersonFetchResult
import com.simprints.id.data.db.PersonFetchResult.PersonSource
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.device.DeviceManager
import kotlinx.coroutines.launch

class FetchGuidViewModel(private val personRepository: PersonRepository,
                         private val deviceManager: DeviceManager,
                         private val sessionEventsManager: SessionEventsManager,
                         private val timeHelper: TimeHelper) : ViewModel() {

    var personFetch = MutableLiveData<PersonSource>()

    fun fetchGuid(projectId: String, verifyGuid: String) {
        viewModelScope.launch {
            val personFetchStartTime = timeHelper.now()
            val personFetchResult = getPersonFetchResult(projectId, verifyGuid)
            personFetch.postValue(personFetchResult.personSource)
            addPersonFetchEventToSession(personFetchResult, personFetchStartTime, verifyGuid)
        }
    }

    private suspend fun getPersonFetchResult(projectId: String, verifyGuid: String) = try {
        personRepository.loadFromRemoteIfNeeded(projectId, verifyGuid)
    } catch (t: Throwable) {
        getPersonFetchResultForError()
    }

    private suspend fun getPersonFetchResultForError() =
        if (deviceManager.isConnected()) {
            PersonFetchResult(null, PersonSource.NOT_FOUND_IN_LOCAL_AND_REMOTE)
        } else {
            PersonFetchResult(null, PersonSource.NOT_FOUND_IN_LOCAL_REMOTE_CONNECTION_ERROR)
        }

    private fun addPersonFetchEventToSession(personFetchResult: PersonFetchResult,
                                             personFetchStartTime: Long,
                                             verifyGuid: String) {
        sessionEventsManager.addEventInBackground(getCandidateReadEvent(personFetchResult,
            personFetchStartTime, verifyGuid))
    }

    private fun getCandidateReadEvent(personFetchResult: PersonFetchResult,
                                      personFetchStartTime: Long,
                                      verifyGuid: String) =
        CandidateReadEvent(personFetchStartTime,
            timeHelper.now(),
            verifyGuid,
            getLocalResultForFetchEvent(personFetchResult.personSource),
            getRemoteResultForFetchEvent(personFetchResult.personSource))

    private fun getLocalResultForFetchEvent(personSource: PersonSource) =
        if (personSource == PersonSource.LOCAL) {
            CandidateReadEvent.LocalResult.FOUND
        } else {
            CandidateReadEvent.LocalResult.NOT_FOUND
        }

    private fun getRemoteResultForFetchEvent(personSource: PersonSource) =
        if (personSource == PersonSource.REMOTE) {
            CandidateReadEvent.RemoteResult.FOUND
        } else {
            CandidateReadEvent.RemoteResult.NOT_FOUND
        }
}
