package com.simprints.face.matcher.usecases

import com.simprints.core.ExternalScope
import com.simprints.core.domain.common.FlowProvider
import com.simprints.face.matcher.MatchParams
import com.simprints.face.matcher.FaceMatchResult
import com.simprints.face.matcher.MatchResultItem
import com.simprints.infra.enrolment.records.domain.models.SubjectQuery
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.MatchEntry
import com.simprints.infra.events.event.domain.models.OneToManyMatchEvent
import com.simprints.infra.events.event.domain.models.OneToOneMatchEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class SaveMatchEventUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    @ExternalScope private val externalScope: CoroutineScope
) {

    operator fun invoke(
        startTime: Long,
        endTime: Long,
        matchParams: MatchParams,
        candidatesCount: Int,
        matcherName: String,
        results: List<MatchResultItem>
    ) {
        externalScope.launch {
            val matchEntries = results.map { MatchEntry(it.guid, it.confidence) }
            val event = if (matchParams.flowType == FlowProvider.FlowType.VERIFY) {
                getOneToOneEvent(startTime, endTime, matcherName, matchParams.queryForCandidates, matchEntries.firstOrNull())
            } else {
                getOneToManyEvent(startTime, endTime, matcherName, matchParams.queryForCandidates, candidatesCount, matchEntries)
            }
            eventRepository.addOrUpdateEvent(event)
        }
    }

    private fun getOneToOneEvent(
        startTime: Long,
        endTime: Long,
        faceMatcherName: String,
        queryForCandidates: SubjectQuery,
        matchEntry: MatchEntry?
    ) = OneToOneMatchEvent(
        startTime,
        endTime,
        queryForCandidates.subjectId!!,
        faceMatcherName,
        matchEntry,
        null
    )

    private fun getOneToManyEvent(
        startTime: Long,
        endTime: Long,
        faceMatcherName: String,
        queryForCandidates: SubjectQuery,
        candidatesCount: Int,
        matchEntries: List<MatchEntry>
    ) = OneToManyMatchEvent(
        startTime,
        endTime,
        OneToManyMatchEvent.OneToManyMatchPayload.MatchPool(
            queryForCandidates.parseQueryAsCoreMatchPoolType(),
            candidatesCount
        ),
        faceMatcherName,
        matchEntries,
    )

    private fun SubjectQuery.parseQueryAsCoreMatchPoolType(): OneToManyMatchEvent.OneToManyMatchPayload.MatchPoolType =
        when {
            this.attendantId != null -> OneToManyMatchEvent.OneToManyMatchPayload.MatchPoolType.USER
            this.moduleId != null -> OneToManyMatchEvent.OneToManyMatchPayload.MatchPoolType.MODULE
            else -> OneToManyMatchEvent.OneToManyMatchPayload.MatchPoolType.PROJECT
        }
}
