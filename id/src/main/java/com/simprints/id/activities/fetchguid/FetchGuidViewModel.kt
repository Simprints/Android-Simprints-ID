package com.simprints.id.activities.fetchguid

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.CandidateReadEvent
import com.simprints.eventsystem.event.domain.models.CandidateReadEvent.CandidateReadPayload.LocalResult
import com.simprints.eventsystem.event.domain.models.CandidateReadEvent.CandidateReadPayload.RemoteResult
import com.simprints.id.data.db.SubjectFetchResult
import com.simprints.id.data.db.SubjectFetchResult.SubjectSource
import com.simprints.id.tools.device.DeviceManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FetchGuidViewModel(
    private val fetchGuidHelper: FetchGuidHelper,
    private val deviceManager: DeviceManager,
    private val eventRepository: EventRepository,
    private val timeHelper: TimeHelper,
    private val dispatcher: DispatcherProvider
) : ViewModel() {

    var subjectFetch = MutableLiveData<SubjectSource>()

    fun fetchGuid(projectId: String, verifyGuid: String) {
        viewModelScope.launch {
            val subjectFetchStartTime = timeHelper.now()
            val subjectFetchResult = getSubjectFetchResult(projectId, verifyGuid)
            subjectFetch.postValue(subjectFetchResult.subjectSource)
            addSubjectFetchEventToSession(subjectFetchResult, subjectFetchStartTime, verifyGuid)
        }
    }

    private suspend fun getSubjectFetchResult(projectId: String, verifyGuid: String) =
        withContext(dispatcher.io()) {
            val fetchResult = fetchGuidHelper.loadFromRemoteIfNeeded(this, projectId, verifyGuid)
            if (fetchResult.subjectSource == SubjectSource.NOT_FOUND_IN_LOCAL_AND_REMOTE)
                getSubjectFetchResultForError()
            else
                fetchResult
        }

    private fun getSubjectFetchResultForError() =
        if (deviceManager.isConnected()) {
            SubjectFetchResult(null, SubjectSource.NOT_FOUND_IN_LOCAL_AND_REMOTE)
        } else {
            SubjectFetchResult(null, SubjectSource.NOT_FOUND_IN_LOCAL_REMOTE_CONNECTION_ERROR)
        }

    private suspend fun addSubjectFetchEventToSession(
        subjectFetchResult: SubjectFetchResult,
        subjectFetchStartTime: Long,
        verifyGuid: String
    ) {
        eventRepository.addOrUpdateEvent(
            getCandidateReadEvent(
                subjectFetchResult,
                subjectFetchStartTime,
                verifyGuid
            )
        )
    }

    private fun getCandidateReadEvent(
        subjectFetchResult: SubjectFetchResult,
        subjectFetchStartTime: Long,
        verifyGuid: String
    ) =
        CandidateReadEvent(
            subjectFetchStartTime,
            timeHelper.now(),
            verifyGuid,
            getLocalResultForFetchEvent(subjectFetchResult.subjectSource),
            getRemoteResultForFetchEvent(subjectFetchResult.subjectSource)
        )

    private fun getLocalResultForFetchEvent(subjectSource: SubjectSource) =
        if (subjectSource == SubjectSource.LOCAL) {
            LocalResult.FOUND
        } else {
            LocalResult.NOT_FOUND
        }

    private fun getRemoteResultForFetchEvent(subjectSource: SubjectSource) =
        when (subjectSource) {
            SubjectSource.LOCAL -> null
            SubjectSource.REMOTE -> RemoteResult.FOUND
            SubjectSource.NOT_FOUND_IN_LOCAL_AND_REMOTE -> RemoteResult.NOT_FOUND
            SubjectSource.NOT_FOUND_IN_LOCAL_REMOTE_CONNECTION_ERROR -> null
        }

}
