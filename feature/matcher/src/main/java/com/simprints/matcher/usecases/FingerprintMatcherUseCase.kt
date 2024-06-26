package com.simprints.matcher.usecases

import com.simprints.core.DispatcherBG
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerIdentifier
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.Fingerprint
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerprintIdentity
import com.simprints.fingerprint.infra.biosdk.BioSdkWrapper
import com.simprints.fingerprint.infra.biosdk.ResolveBioSdkWrapperUseCase
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration.FingerComparisonStrategy.CROSS_FINGER_USING_MEAN_OF_MAX
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.store.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.store.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
import com.simprints.infra.logging.LoggingConstants
import com.simprints.matcher.FingerprintMatchResult
import com.simprints.matcher.MatchParams
import com.simprints.matcher.usecases.MatcherUseCase.MatcherResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

internal class FingerprintMatcherUseCase @Inject constructor(
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val resolveBioSdkWrapper: ResolveBioSdkWrapperUseCase,
    private val configManager: ConfigManager,
    private val createRanges: CreateRangesUseCase,
    @DispatcherBG private val dispatcher: CoroutineDispatcher,
) : MatcherUseCase {

    override val crashReportTag = LoggingConstants.CrashReportTag.MATCHING.name

    override suspend operator fun invoke(
        matchParams: MatchParams,
        onLoadingCandidates: (tag: String) -> Unit,
    ): MatcherResult = coroutineScope {
        val bioSdkWrapper = resolveBioSdkWrapper(matchParams.fingerprintSDK!!)

        if (matchParams.probeFingerprintSamples.isEmpty()) {
            return@coroutineScope MatcherResult(emptyList(), 0, bioSdkWrapper.matcherName)
        }
        val samples = mapSamples(matchParams.probeFingerprintSamples)
        // Only candidates with supported template format are considered
        val queryWithSupportedFormat =
            matchParams.queryForCandidates.copy(
                fingerprintSampleFormat = bioSdkWrapper.supportedTemplateFormat
            )
        val totalCandidates = enrolmentRecordRepository.count(queryWithSupportedFormat, dataSource = matchParams.biometricDataSource)
        if (totalCandidates == 0) {
            return@coroutineScope MatcherResult(emptyList(), 0, bioSdkWrapper.matcherName)
        }

        onLoadingCandidates(crashReportTag)
        val resultItems = createRanges(totalCandidates)
            .map { range ->
                async(dispatcher) {
                    val batchCandidates = getCandidates(queryWithSupportedFormat, range, matchParams.biometricDataSource)
                    match(samples, batchCandidates, matchParams.flowType, bioSdkWrapper, bioSdk = matchParams.fingerprintSDK)
                        .fold(MatchResultSet<FingerprintMatchResult.Item>()) { acc, item ->
                            acc.add(FingerprintMatchResult.Item(item.id, item.score))
                        }
                }
            }
            .awaitAll()
            .reduce { acc, subSet -> acc.addAll(subSet) }
            .toList()
        MatcherResult(resultItems, totalCandidates, bioSdkWrapper.matcherName)
    }

    private fun mapSamples(probes: List<MatchParams.FingerprintSample>) = probes
        .map { Fingerprint(it.fingerId.toMatcherDomain(), it.template, it.format) }

    private suspend fun getCandidates(
        query: SubjectQuery,
        range: IntRange,
        dataSource: BiometricDataSource = BiometricDataSource.Simprints,
    ) = enrolmentRecordRepository
        .loadFingerprintIdentities(query, range, dataSource)
        .map {
            FingerprintIdentity(
                it.subjectId,
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
        bioSdkWrapper: BioSdkWrapper,
        bioSdk: FingerprintConfiguration.BioSdk,
    ) = bioSdkWrapper.match(
        FingerprintIdentity("", probes),
        candidates,
        isCrossFingerMatchingEnabled(flowType, bioSdk),
    )

    private suspend fun isCrossFingerMatchingEnabled(
        flowType: FlowType,
        bioSdk: FingerprintConfiguration.BioSdk,
    ): Boolean = configManager
        .takeIf { flowType == FlowType.VERIFY }
        ?.getProjectConfiguration()
        ?.fingerprint
        ?.getSdkConfiguration(bioSdk)
        ?.comparisonStrategyForVerification == CROSS_FINGER_USING_MEAN_OF_MAX

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


}
