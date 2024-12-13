package com.simprints.feature.fetchsubject.screen.usecase

import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.fetchsubject.screen.FetchSubjectState
import com.simprints.infra.events.event.domain.models.CandidateReadEvent
import com.simprints.infra.events.session.SessionEventRepository
import javax.inject.Inject

internal class SaveSubjectFetchEventUseCase @Inject constructor(
    private val eventRepository: SessionEventRepository,
) {
    suspend operator fun invoke(
        subjectState: FetchSubjectState,
        fetchStartTime: Timestamp,
        fetchEndTime: Timestamp,
        subjectId: String,
    ) {
        eventRepository.addOrUpdateEvent(
            CandidateReadEvent(
                createdAt = fetchStartTime,
                endTime = fetchEndTime,
                candidateId = subjectId,
                localResult = getLocalResultForFetchEvent(subjectState),
                remoteResult = getRemoteResultForFetchEvent(subjectState),
            ),
        )
    }

    private fun getLocalResultForFetchEvent(state: FetchSubjectState) = when (state) {
        FetchSubjectState.FoundLocal -> CandidateReadEvent.CandidateReadPayload.LocalResult.FOUND
        else -> CandidateReadEvent.CandidateReadPayload.LocalResult.NOT_FOUND
    }

    private fun getRemoteResultForFetchEvent(state: FetchSubjectState) = when (state) {
        FetchSubjectState.FoundRemote -> CandidateReadEvent.CandidateReadPayload.RemoteResult.FOUND
        FetchSubjectState.NotFound -> CandidateReadEvent.CandidateReadPayload.RemoteResult.NOT_FOUND
        else -> null
    }
}
