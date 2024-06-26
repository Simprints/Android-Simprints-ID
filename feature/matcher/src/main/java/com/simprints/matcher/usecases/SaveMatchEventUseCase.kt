package com.simprints.matcher.usecases

import com.simprints.core.ExternalScope
import com.simprints.core.domain.common.FlowType
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
import com.simprints.infra.events.SessionEventRepository
import com.simprints.infra.events.event.domain.models.FingerComparisonStrategy
import com.simprints.infra.events.event.domain.models.MatchEntry
import com.simprints.infra.events.event.domain.models.OneToManyMatchEvent
import com.simprints.infra.events.event.domain.models.OneToOneMatchEvent
import com.simprints.matcher.MatchParams
import com.simprints.matcher.MatchResultItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.simprints.infra.config.store.models.FingerprintConfiguration.FingerComparisonStrategy as ConfigFingerComparisonStrategy

internal class SaveMatchEventUseCase @Inject constructor(
    private val eventRepository: SessionEventRepository,
    private val configManager: ConfigManager,
    @ExternalScope private val externalScope: CoroutineScope,
) {

    operator fun invoke(
        startTime: Timestamp,
        endTime: Timestamp,
        matchParams: MatchParams,
        candidatesCount: Int,
        matcherName: String,
        results: List<MatchResultItem>
    ) {
        externalScope.launch {
            val matchEntries = results.map { MatchEntry(it.subjectId, it.confidence) }
            val event = if (matchParams.flowType == FlowType.VERIFY) {
                getOneToOneEvent(
                    startTime, endTime,
                    matcherName,
                    matchParams.queryForCandidates,
                    matchEntries.firstOrNull(),
                    if (matchParams.isFaceMatch()) null else getFingerprintComparisonStrategy(matchParams.fingerprintSDK!!),
                )
            } else {
                getOneToManyEvent(
                    startTime, endTime,
                    matcherName,
                    matchParams.queryForCandidates,
                    candidatesCount,
                    matchEntries
                )
            }
            eventRepository.addOrUpdateEvent(event)
        }
    }

    private suspend fun getFingerprintComparisonStrategy(bioSdk: FingerprintConfiguration.BioSdk) =
        configManager.getProjectConfiguration()
        .fingerprint
        ?.getSdkConfiguration(bioSdk)
        ?.comparisonStrategyForVerification
        ?.let {
            when (it) {
                ConfigFingerComparisonStrategy.SAME_FINGER -> FingerComparisonStrategy.SAME_FINGER
                ConfigFingerComparisonStrategy.CROSS_FINGER_USING_MEAN_OF_MAX -> FingerComparisonStrategy.CROSS_FINGER_USING_MEAN_OF_MAX
            }
        }

    private fun getOneToOneEvent(
        startTime: Timestamp,
        endTime: Timestamp,
        matcherName: String,
        queryForCandidates: SubjectQuery,
        matchEntry: MatchEntry?,
        fingerComparisonStrategy: FingerComparisonStrategy?,
    ) = OneToOneMatchEvent(
        startTime,
        endTime,
        queryForCandidates.subjectId!!,
        matcherName,
        matchEntry,
        fingerComparisonStrategy,
    )

    private fun getOneToManyEvent(
        startTime: Timestamp,
        endTime: Timestamp,
        matcherName: String,
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
        matcherName,
        matchEntries,
    )

    private fun SubjectQuery.parseQueryAsCoreMatchPoolType(): OneToManyMatchEvent.OneToManyMatchPayload.MatchPoolType =
        when {
            this.attendantId != null -> OneToManyMatchEvent.OneToManyMatchPayload.MatchPoolType.USER
            this.moduleId != null -> OneToManyMatchEvent.OneToManyMatchPayload.MatchPoolType.MODULE
            else -> OneToManyMatchEvent.OneToManyMatchPayload.MatchPoolType.PROJECT
        }
}
