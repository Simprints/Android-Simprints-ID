package com.simprints.matcher.usecases

import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerIdentifier
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.Fingerprint
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerprintIdentity
import com.simprints.fingerprint.infra.biosdk.BioSdkWrapper
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.sync.EnrolmentRecordManager
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.Simber
import com.simprints.matcher.FingerprintMatchResult
import com.simprints.matcher.MatchParams
import com.simprints.matcher.MatchResultItem
import java.io.Serializable
import javax.inject.Inject
import kotlin.time.TimedValue
import kotlin.time.measureTimedValue

internal class FingerprintMatcherUseCase @Inject constructor(
    private val enrolmentRecordManager: EnrolmentRecordManager,
    private val bioSdkWrapper: BioSdkWrapper,
    private val configManager: ConfigManager,
) : MatcherUseCase {

    override val crashReportTag = LoggingConstants.CrashReportTag.MATCHING.name
    override val matcherName = MATCHER_NAME

    override suspend operator fun invoke(
        matchParams: MatchParams,
        onLoadingCandidates: (tag: String) -> Unit,
        onMatching: (tag: String) -> Unit,
    ): List<MatchResultItem> {
        val candidates: TimedValue<List<FingerprintIdentity>>
        val matching: TimedValue<List<FingerprintMatchResult.Item>>

        val totalDuration = measureTimedValue {
            if (matchParams.probeFingerprintSamples.isEmpty()) {
                return emptyList()
            }
            val samples = mapSamples(matchParams.probeFingerprintSamples)

            onLoadingCandidates(crashReportTag)
            candidates = measureTimedValue { getCandidates(matchParams.queryForCandidates) }

            onMatching(crashReportTag)
            matching = measureTimedValue { match(samples, candidates.value, matchParams.flowType) }
            matching.value
        }

        // TODO remove this benchmarking code when we are confident in the performance of the matcher
        Simber.d("BENCHMARK: ===========================================")
        Simber.d("BENCHMARK: module ${matchParams.queryForCandidates.moduleId}")
        Simber.d("BENCHMARK: \tReading\tMatching\tTotal")
        Simber.d("BENCHMARK: \t${candidates.duration.inWholeMilliseconds}\t${matching.duration.inWholeMilliseconds}\t${totalDuration.duration.inWholeMilliseconds}")
        Simber.d("BENCHMARK: ===========================================")
        return totalDuration.value
    }

    private fun mapSamples(probes: List<MatchParams.FingerprintSample>) = probes
        .map { Fingerprint(it.fingerId.toMatcherDomain(), it.template, it.format) }

    private suspend fun getCandidates(query: Serializable) = enrolmentRecordManager
        .loadFingerprintIdentities(query)
        .map {
            FingerprintIdentity(
                it.patientId,
                it.fingerprints.map { finger ->
                    Fingerprint(
                        finger.fingerIdentifier.toMatcherDomain(),
                        finger.template,
                        finger.format,
                    )
                }
            )
        }

    private suspend fun match(
        probes: List<Fingerprint>,
        candidates: List<FingerprintIdentity>,
        flowType: FlowType,
    ) = bioSdkWrapper
        .match(
            FingerprintIdentity("", probes),
            candidates,
            isCrossFingerMatchingEnabled(flowType),
        )
        .map { FingerprintMatchResult.Item(it.id, it.score) }
        .sortedByDescending { it.confidence }

    private suspend fun isCrossFingerMatchingEnabled(flowType: FlowType): Boolean = configManager
        .takeIf { flowType == FlowType.VERIFY }
        ?.getProjectConfiguration()
        ?.fingerprint
        ?.comparisonStrategyForVerification == FingerprintConfiguration.FingerComparisonStrategy.CROSS_FINGER_USING_MEAN_OF_MAX

    private fun IFingerIdentifier.toMatcherDomain() = when (this) {
        IFingerIdentifier.RIGHT_5TH_FINGER -> FingerIdentifier.RIGHT_5TH_FINGER
        IFingerIdentifier.RIGHT_4TH_FINGER -> FingerIdentifier.RIGHT_4TH_FINGER
        IFingerIdentifier.RIGHT_3RD_FINGER -> FingerIdentifier.RIGHT_3RD_FINGER
        IFingerIdentifier.RIGHT_INDEX_FINGER -> FingerIdentifier.RIGHT_INDEX_FINGER
        IFingerIdentifier.RIGHT_THUMB -> FingerIdentifier.RIGHT_THUMB
        IFingerIdentifier.LEFT_THUMB -> FingerIdentifier.LEFT_THUMB
        IFingerIdentifier.LEFT_INDEX_FINGER -> FingerIdentifier.LEFT_INDEX_FINGER
        IFingerIdentifier.LEFT_3RD_FINGER -> FingerIdentifier.LEFT_3RD_FINGER
        IFingerIdentifier.LEFT_4TH_FINGER -> FingerIdentifier.LEFT_4TH_FINGER
        IFingerIdentifier.LEFT_5TH_FINGER -> FingerIdentifier.LEFT_5TH_FINGER
    }

    companion object {

        private const val MATCHER_NAME = "SIM_AFIS"
    }
}
