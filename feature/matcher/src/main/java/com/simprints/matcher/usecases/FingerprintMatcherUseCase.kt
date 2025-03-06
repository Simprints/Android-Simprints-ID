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
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.Simber
import com.simprints.matcher.FingerprintMatchResult
import com.simprints.matcher.MatchParams
import com.simprints.matcher.usecases.MatcherUseCase.MatcherState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import javax.inject.Inject

internal class FingerprintMatcherUseCase @Inject constructor(
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val resolveBioSdkWrapper: ResolveBioSdkWrapperUseCase,
    private val configManager: ConfigManager,
    private val createRanges: CreateRangesUseCase,
    @DispatcherBG private val dispatcher: CoroutineDispatcher,
) : MatcherUseCase {
    override val crashReportTag = LoggingConstants.CrashReportTag.FINGER_MATCHING

    override suspend operator fun invoke(
        matchParams: MatchParams,
        project: Project,
    ): Flow<MatcherState> = channelFlow {
        Simber.i("Initialising matcher", tag = crashReportTag)
        val bioSdkWrapper = resolveBioSdkWrapper(matchParams.fingerprintSDK!!)

        if (matchParams.probeFingerprintSamples.isEmpty()) {
            send(MatcherState.Success(emptyList(), 0, bioSdkWrapper.matcherName))
            return@channelFlow
        }
        val samples = mapSamples(matchParams.probeFingerprintSamples)
        // Only candidates with supported template format are considered
        val queryWithSupportedFormat =
            matchParams.queryForCandidates.copy(
                fingerprintSampleFormat = bioSdkWrapper.supportedTemplateFormat,
            )
        val expectedCandidates = enrolmentRecordRepository.count(queryWithSupportedFormat, dataSource = matchParams.biometricDataSource)
        if (expectedCandidates == 0) {
            send(MatcherState.Success(emptyList(), 0, bioSdkWrapper.matcherName))
            return@channelFlow
        }

        Simber.i("Matching candidates", tag = crashReportTag)
        send(MatcherState.LoadingStarted(expectedCandidates))
        // When using local DB loadedCandidates = expectedCandidates
        // However, when using CommCare as data source, loadedCandidates < expectedCandidates
        // as it's count function does not take into account filtering criteria
        var loadedCandidates = 0
        val resultItems = createRanges(expectedCandidates)
            .map { range ->
                async(dispatcher) {
                    val batchCandidates = getCandidates(queryWithSupportedFormat, range, matchParams.biometricDataSource, project) {
                        // When a candidate is loaded
                        loadedCandidates++
                        trySend(MatcherState.CandidateLoaded)
                    }
                    match(samples, batchCandidates, matchParams.flowType, bioSdkWrapper, bioSdk = matchParams.fingerprintSDK)
                        .fold(MatchResultSet<FingerprintMatchResult.Item>()) { acc, item ->
                            acc.add(FingerprintMatchResult.Item(item.id, item.score))
                        }
                }
            }.awaitAll()
            .reduce { acc, subSet -> acc.addAll(subSet) }
            .toList()

        Simber.i("Matched $loadedCandidates candidates", tag = crashReportTag)
        send(MatcherState.Success(resultItems, loadedCandidates, bioSdkWrapper.matcherName))
    }

    private fun mapSamples(probes: List<MatchParams.FingerprintSample>) = probes
        .map { Fingerprint(it.fingerId.toMatcherDomain(), it.template, it.format) }

    private suspend fun getCandidates(
        query: SubjectQuery,
        range: IntRange,
        dataSource: BiometricDataSource = BiometricDataSource.Simprints,
        project: Project,
        onCandidateLoaded: () -> Unit,
    ) = enrolmentRecordRepository
        .loadFingerprintIdentities(query, range, dataSource, project, onCandidateLoaded)
        .map {
            FingerprintIdentity(
                it.subjectId,
                it.fingerprints.map { finger ->
                    Fingerprint(
                        finger.fingerIdentifier.toMatcherDomain(),
                        finger.template,
                        finger.format,
                    )
                },
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
