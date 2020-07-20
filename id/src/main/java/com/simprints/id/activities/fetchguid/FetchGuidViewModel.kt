package com.simprints.id.activities.fetchguid

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.tools.extentions.inBackground
import com.simprints.id.data.db.SubjectFetchResult
import com.simprints.id.data.db.SubjectFetchResult.SubjectSource
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.event.domain.models.CandidateReadEvent
import com.simprints.id.data.db.event.domain.models.CandidateReadEvent.CandidateReadPayload.LocalResult
import com.simprints.id.data.db.event.domain.models.CandidateReadEvent.CandidateReadPayload.RemoteResult
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.device.DeviceManager
import kotlinx.coroutines.launch

class FetchGuidViewModel(private val subjectRepository: SubjectRepository,
                         private val deviceManager: DeviceManager,
                         private val eventRepository: EventRepository,
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
        inBackground {
            eventRepository.addEvent(getCandidateReadEvent(subjectFetchResult, subjectFetchStartTime, verifyGuid))
        }
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
            LocalResult.FOUND
        } else {
            LocalResult.NOT_FOUND
        }

    private fun getRemoteResultForFetchEvent(subjectSource: SubjectSource) =
        if (subjectSource == SubjectSource.REMOTE) {
            RemoteResult.FOUND
        } else {
            RemoteResult.NOT_FOUND
        }
}
