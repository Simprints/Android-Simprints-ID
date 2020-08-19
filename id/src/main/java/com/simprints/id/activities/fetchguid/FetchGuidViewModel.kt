package com.simprints.id.activities.fetchguid

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.id.data.db.SubjectFetchResult
import com.simprints.id.data.db.SubjectFetchResult.SubjectSource
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.db.session.domain.models.events.CandidateReadEvent
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.tools.time.TimeHelper
import com.simprints.id.tools.device.DeviceManager
import kotlinx.coroutines.launch

class FetchGuidViewModel(private val subjectRepository: SubjectRepository,
                         private val deviceManager: DeviceManager,
                         private val sessionRepository: SessionRepository,
                         private val timeHelper: TimeHelper) : ViewModel() {

    var subjectFetch = MutableLiveData<SubjectSource>()

    fun fetchGuid(projectId: String, verifyGuid: String) {
        viewModelScope.launch {
            val subjectFetchStartTime = timeHelper.now()
            val subjectFetchResult = getSubjectFetchResult(projectId, verifyGuid)
            subjectFetch.postValue(subjectFetchResult.subjectSource)
            addSubjectFetchEventToSession(subjectFetchResult, subjectFetchStartTime, verifyGuid)
        }
    }

    private suspend fun getSubjectFetchResult(projectId: String, verifyGuid: String) = try {
        subjectRepository.loadFromRemoteIfNeeded(projectId, verifyGuid)
    } catch (t: Throwable) {
        getSubjectFetchResultForError()
    }

    private fun getSubjectFetchResultForError() =
        if (deviceManager.isConnected()) {
            SubjectFetchResult(null, SubjectSource.NOT_FOUND_IN_LOCAL_AND_REMOTE)
        } else {
            SubjectFetchResult(null, SubjectSource.NOT_FOUND_IN_LOCAL_REMOTE_CONNECTION_ERROR)
        }

    private fun addSubjectFetchEventToSession(subjectFetchResult: SubjectFetchResult,
                                              subjectFetchStartTime: Long,
                                              verifyGuid: String) {
        sessionRepository.addEventToCurrentSessionInBackground(getCandidateReadEvent(subjectFetchResult,
            subjectFetchStartTime, verifyGuid))
    }

    private fun getCandidateReadEvent(subjectFetchResult: SubjectFetchResult,
                                      subjectFetchStartTime: Long,
                                      verifyGuid: String) =
        CandidateReadEvent(subjectFetchStartTime,
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
