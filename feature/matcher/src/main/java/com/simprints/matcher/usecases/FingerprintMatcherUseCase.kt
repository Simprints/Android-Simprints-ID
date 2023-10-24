package com.simprints.matcher.usecases

import com.simprints.core.domain.common.FlowProvider
import com.simprints.matcher.FingerprintMatchResult
import com.simprints.matcher.MatchParams
import com.simprints.matcher.MatchResultItem
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerIdentifier
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.Fingerprint
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerprintIdentity
import com.simprints.fingerprint.infra.biosdk.BioSdkWrapper
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.sync.EnrolmentRecordManager
import com.simprints.infra.logging.LoggingConstants
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.io.Serializable
import javax.inject.Inject

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
        if (matchParams.probeFingerprintSamples.isEmpty()) {
            return emptyList()
        }

        val samples = mapSamples(matchParams.probeFingerprintSamples)

        onLoadingCandidates(crashReportTag)
        val candidates = getCandidates(matchParams.queryForCandidates)

        onMatching(crashReportTag)
        return match(samples, candidates, matchParams.flowType)
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
        .toList()

    private suspend fun match(
        probes: List<Fingerprint>,
        candidates: List<FingerprintIdentity>,
        flowType: FlowProvider.FlowType,
    ) = bioSdkWrapper
        .match(
            FingerprintIdentity("", probes),
            candidates,
            isCrossFingerMatchingEnabled(flowType),
        )
        .map { FingerprintMatchResult.Item(it.id, it.score) }
        .sortedByDescending { it.confidence }

    private suspend fun isCrossFingerMatchingEnabled(flowType: FlowProvider.FlowType): Boolean = configManager
        .takeIf { flowType == FlowProvider.FlowType.VERIFY }
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
