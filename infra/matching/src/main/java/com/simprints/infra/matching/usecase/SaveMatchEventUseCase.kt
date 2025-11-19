package com.simprints.infra.matching.usecase

import com.simprints.core.SessionCoroutineScope
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.sample.MatchComparisonResult
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.events.event.domain.models.FingerComparisonStrategy
import com.simprints.infra.events.event.domain.models.MatchEntry
import com.simprints.infra.events.event.domain.models.OneToManyMatchEvent
import com.simprints.infra.events.event.domain.models.OneToOneMatchEvent
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.matching.MatchBatchInfo
import com.simprints.infra.matching.MatchParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.simprints.infra.config.store.models.FingerprintConfiguration.FingerComparisonStrategy as ConfigFingerComparisonStrategy

class SaveMatchEventUseCase @Inject constructor(
    private val eventRepository: SessionEventRepository,
    private val configManager: ConfigManager,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) {
    operator fun invoke(
        startTime: Timestamp,
        endTime: Timestamp,
        matchParams: MatchParams,
        candidatesCount: Int,
        matcherName: String,
        results: List<MatchComparisonResult>,
        batches: List<MatchBatchInfo>,
    ) {
        sessionCoroutineScope.launch {
            val matchEntries = results.map { MatchEntry(it.subjectId, it.confidence) }
            val event = if (matchParams.flowType == FlowType.VERIFY) {
                getOneToOneEvent(
                    startTime,
                    endTime,
                    matcherName,
                    matchParams.queryForCandidates,
                    matchEntries.firstOrNull(),
                    matchParams.bioSdk
                        .let { it as? FingerprintConfiguration.BioSdk }
                        ?.let { getFingerprintComparisonStrategy(it) },
                    matchParams.probeReferenceId,
                )
            } else {
                getOneToManyEvent(
                    startTime,
                    endTime,
                    matcherName,
                    matchParams.queryForCandidates,
                    candidatesCount,
                    matchEntries,
                    matchParams.probeReferenceId,
                    batches,
                )
            }
            eventRepository.addOrUpdateEvent(event)
        }
    }

    private suspend fun getFingerprintComparisonStrategy(bioSdk: FingerprintConfiguration.BioSdk) = configManager
        .getProjectConfiguration()
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
        biometricReferenceId: String,
    ) = OneToOneMatchEvent(
        createdAt = startTime,
        endTime = endTime,
        candidateId = queryForCandidates.subjectId!!,
        matcher = matcherName,
        result = matchEntry,
        fingerComparisonStrategy = fingerComparisonStrategy,
        probeBiometricReferenceId = biometricReferenceId,
    )

    private fun getOneToManyEvent(
        startTime: Timestamp,
        endTime: Timestamp,
        matcherName: String,
        queryForCandidates: SubjectQuery,
        candidatesCount: Int,
        matchEntries: List<MatchEntry>,
        biometricReferenceId: String,
        batches: List<MatchBatchInfo>,
    ) = OneToManyMatchEvent(
        createdAt = startTime,
        endTime = endTime,
        pool = OneToManyMatchEvent.OneToManyMatchPayload.MatchPool(
            queryForCandidates.parseQueryAsCoreMatchPoolType(),
            candidatesCount,
        ),
        matcher = matcherName,
        result = matchEntries,
        probeBiometricReferenceId = biometricReferenceId,
        batches = batches.map {
            OneToManyMatchEvent.OneToManyMatchPayload.OneToManyBatch(
                loadingStartTime = it.loadingStartTime,
                loadingEndTime = it.loadingEndTime,
                comparingStartTime = it.comparingStartTime,
                comparingEndTime = it.comparingEndTime,
                count = it.count,
            )
        },
    )

    private fun SubjectQuery.parseQueryAsCoreMatchPoolType(): OneToManyMatchEvent.OneToManyMatchPayload.MatchPoolType = when {
        this.attendantId != null -> OneToManyMatchEvent.OneToManyMatchPayload.MatchPoolType.USER
        this.moduleId != null -> OneToManyMatchEvent.OneToManyMatchPayload.MatchPoolType.MODULE
        else -> OneToManyMatchEvent.OneToManyMatchPayload.MatchPoolType.PROJECT
    }
}
