package com.simprints.matcher.usecases

import com.simprints.core.AvailableProcessors
import com.simprints.core.DispatcherBG
import com.simprints.core.DispatcherIO
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
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.Simber
import com.simprints.matcher.FingerprintMatchResult
import com.simprints.matcher.MatchParams
import com.simprints.matcher.usecases.MatcherUseCase.MatcherState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlin.math.min
import com.simprints.infra.enrolment.records.repository.domain.models.FingerprintIdentity as DomainFingerprintIdentity

internal class FingerprintMatcherUseCase @Inject constructor(
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val resolveBioSdkWrapper: ResolveBioSdkWrapperUseCase,
    private val configManager: ConfigManager,
    private val createRanges: CreateRangesUseCase,
    @AvailableProcessors private val availableProcessors: Int,
    @DispatcherBG private val dispatcherBG: CoroutineDispatcher,
    @DispatcherIO private val dispatcherIO: CoroutineDispatcher,
) : MatcherUseCase {
    override val crashReportTag = LoggingConstants.CrashReportTag.FINGER_MATCHING

    override suspend operator fun invoke(
        matchParams: MatchParams,
        project: Project,
    ): Flow<MatcherState> = channelFlow {
        Simber.i("Initialising matcher", tag = crashReportTag)
        if (matchParams.fingerprintSDK == null) {
            Simber.w("Fingerprint SDK was not provided", tag = crashReportTag)
            send(MatcherState.Success(emptyList(), 0, ""))
            return@channelFlow
        }
        val bioSdkWrapper = resolveBioSdkWrapper(matchParams.fingerprintSDK)

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
        val loadedCandidates = AtomicInteger(0)
        val ranges = createRanges(expectedCandidates)
        // if number of ranges less than the number of cores then use the number of ranges
        val numConsumers = min(availableProcessors, ranges.size)
        val channel = enrolmentRecordRepository.loadFingerprintIdentities(
            query = queryWithSupportedFormat,
            ranges = ranges,
            dataSource = matchParams.biometricDataSource,
            scope = this,
            project = project,
        ) {
            loadedCandidates.incrementAndGet()
            trySend(MatcherState.CandidateLoaded)
        }

        val resultSet = MatchResultSet<FingerprintMatchResult.Item>()

        // Start Consumers in BG thread
        val consumerJobs = List(numConsumers) {
            launch(dispatcherBG) {
                consumeAndMatch(channel, samples, resultSet, bioSdkWrapper, matchParams)
            }
        }
        // Wait for all to complete
        consumerJobs.forEach { it.join() }

        Simber.i("Matched $loadedCandidates candidates", tag = crashReportTag)
        send(MatcherState.Success(resultSet.toList(), loadedCandidates.get(), bioSdkWrapper.matcherName))
    }.flowOn(dispatcherIO)

    private suspend fun consumeAndMatch(
        channel: ReceiveChannel<List<DomainFingerprintIdentity>>,
        samples: List<Fingerprint>,
        resultSet: MatchResultSet<FingerprintMatchResult.Item>,
        bioSdkWrapper: BioSdkWrapper,
        matchParams: MatchParams,
    ) {
        for (batch in channel) {
            val matchResults =
                match(samples, batch.mapToFingerprintIdentity(), matchParams.flowType, bioSdkWrapper, bioSdk = matchParams.fingerprintSDK!!)
                    .fold(MatchResultSet<FingerprintMatchResult.Item>()) { acc, item ->
                        acc.add(FingerprintMatchResult.Item(item.id, item.score))
                    }
            resultSet.addAll(matchResults)
        }
    }

    private fun mapSamples(probes: List<MatchParams.FingerprintSample>) = probes
        .map { Fingerprint(it.fingerId.toMatcherDomain(), it.template, it.format) }

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

    private fun List<DomainFingerprintIdentity>.mapToFingerprintIdentity() = map {
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
}
