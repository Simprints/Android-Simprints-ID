package com.simprints.id.activities.fetchguid

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.id.data.db.SubjectFetchResult
import com.simprints.id.data.db.SubjectFetchResult.SubjectSource
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.db.session.domain.models.events.CandidateReadEvent
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.device.DeviceManager
import kotlinx.coroutines.launch

class FetchGuidViewModel(private val personRepository: SubjectRepository,
                         private val deviceManager: DeviceManager,
                         private val sessionRepository: SessionRepository,
                         private val timeHelper: TimeHelper) : ViewModel() {

    var personFetch = MutableLiveData<SubjectSource>()

    fun fetchGuid(projectId: String, verifyGuid: String) {
        viewModelScope.launch {
            val personFetchStartTime = timeHelper.now()
            val personFetchResult = getPersonFetchResult(projectId, verifyGuid)
            personFetch.postValue(personFetchResult.subjectSource)
            addPersonFetchEventToSession(personFetchResult, personFetchStartTime, verifyGuid)
        }
    }

    private suspend fun getPersonFetchResult(projectId: String, verifyGuid: String) = try {
        personRepository.loadFromRemoteIfNeeded(projectId, verifyGuid)
    } catch (t: Throwable) {
        getPersonFetchResultForError()
    }

    private fun getPersonFetchResultForError() =
        if (deviceManager.isConnected()) {
            SubjectFetchResult(null, SubjectSource.NOT_FOUND_IN_LOCAL_AND_REMOTE)
        } else {
            SubjectFetchResult(null, SubjectSource.NOT_FOUND_IN_LOCAL_REMOTE_CONNECTION_ERROR)
        }

    private fun addPersonFetchEventToSession(subjectFetchResult: SubjectFetchResult,
                                             personFetchStartTime: Long,
                                             verifyGuid: String) {
        sessionRepository.addEventToCurrentSessionInBackground(getCandidateReadEvent(subjectFetchResult,
            personFetchStartTime, verifyGuid))
    }

    private fun getCandidateReadEvent(subjectFetchResult: SubjectFetchResult,
                                      personFetchStartTime: Long,
                                      verifyGuid: String) =
        CandidateReadEvent(personFetchStartTime,
            timeHelper.now(),
            verifyGuid,
            getLocalResultForFetchEvent(subjectFetchResult.subjectSource),
            getRemoteResultForFetchEvent(subjectFetchResult.subjectSource))

    private fun getLocalResultForFetchEvent(subjectSource: SubjectSource) =
        if (subjectSource == SubjectSource.LOCAL) {
            CandidateReadEvent.LocalResult.FOUND
        } else {
            CandidateReadEvent.LocalResult.NOT_FOUND
        }

    private fun getRemoteResultForFetchEvent(subjectSource: SubjectSource) =
        if (subjectSource == SubjectSource.REMOTE) {
            CandidateReadEvent.RemoteResult.FOUND
        } else {
            CandidateReadEvent.RemoteResult.NOT_FOUND
        }
}
