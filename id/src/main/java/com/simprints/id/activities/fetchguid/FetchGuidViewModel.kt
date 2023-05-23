package com.simprints.id.activities.fetchguid

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.tools.time.TimeHelper
import com.simprints.id.data.db.SubjectFetchResult
import com.simprints.id.data.db.SubjectFetchResult.SubjectSource
import com.simprints.id.exitformhandler.ExitFormHelper
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.CandidateReadEvent
import com.simprints.infra.events.event.domain.models.CandidateReadEvent.CandidateReadPayload.LocalResult
import com.simprints.infra.events.event.domain.models.CandidateReadEvent.CandidateReadPayload.RemoteResult
import com.simprints.infra.network.ConnectivityTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FetchGuidViewModel @Inject constructor(
    private val fetchGuidHelper: FetchGuidHelper,
    private val connectivityTracker: ConnectivityTracker,
    private val eventRepository: EventRepository,
    private val timeHelper: TimeHelper,
    private val configManager: ConfigManager,
    private val exitFormHelper: ExitFormHelper,
) : ViewModel() {

    var subjectFetch = MutableLiveData<SubjectSource>()
    var exitForm = MutableLiveData<LiveDataEventWithContent<Bundle?>>()

    fun fetchGuid(projectId: String, verifyGuid: String) {
        viewModelScope.launch {
            val subjectFetchStartTime = timeHelper.now()
            val subjectFetchResult = getSubjectFetchResult(projectId, verifyGuid)
            subjectFetch.postValue(subjectFetchResult.subjectSource)
            addSubjectFetchEventToSession(subjectFetchResult, subjectFetchStartTime, verifyGuid)
        }
    }

    private suspend fun getSubjectFetchResult(projectId: String, verifyGuid: String): SubjectFetchResult {
        val fetchResult = fetchGuidHelper.loadFromRemoteIfNeeded(projectId, verifyGuid)
        return if (fetchResult.subjectSource == SubjectSource.NOT_FOUND_IN_LOCAL_AND_REMOTE)
            getSubjectFetchResultForError()
        else
            fetchResult
    }

    private fun getSubjectFetchResultForError() =
        if (connectivityTracker.isConnected()) {
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

    fun startExitForm() {
        viewModelScope.launch {
            val modalities = configManager.getProjectConfiguration().general.modalities
            val formArgs = exitFormHelper.getExitFormFromModalities(modalities)
            exitForm.postValue(LiveDataEventWithContent(formArgs))
        }
    }
}
