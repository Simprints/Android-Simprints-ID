package com.simprints.matcher.usecases

import com.simprints.core.DispatcherBG
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.modality.Modality
import com.simprints.core.domain.sample.CaptureIdentity
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.core.domain.sample.Identity
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
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

internal class FingerprintMatcherUseCase @Inject constructor(
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val resolveBioSdkWrapper: ResolveBioSdkWrapperUseCase,
    private val configManager: ConfigManager,
    private val createRanges: CreateRangesUseCase,
    @DispatcherBG private val dispatcherBG: CoroutineDispatcher,
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

        if (matchParams.probeSamples.isEmpty()) {
            send(MatcherState.Success(emptyList(), 0, bioSdkWrapper.matcherName))
            return@channelFlow
        }

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
        val channel = enrolmentRecordRepository.loadFingerprintIdentities(
            query = queryWithSupportedFormat,
            ranges = ranges,
            dataSource = matchParams.biometricDataSource,
            scope = this,
            project = project,
        ) {
            loadedCandidates.incrementAndGet()
            this@channelFlow.send(MatcherState.CandidateLoaded)
        }

        val resultSet = MatchResultSet<FingerprintMatchResult.Item>()

        consumeAndMatch(channel, matchParams.probeSamples, resultSet, bioSdkWrapper, matchParams)

        Simber.i("Matched $loadedCandidates candidates", tag = crashReportTag)
        send(MatcherState.Success(resultSet.toList(), loadedCandidates.get(), bioSdkWrapper.matcherName))
    }.flowOn(dispatcherBG)

    private suspend fun consumeAndMatch(
        channel: ReceiveChannel<List<Identity>>,
        samples: List<CaptureSample>,
        resultSet: MatchResultSet<FingerprintMatchResult.Item>,
        bioSdkWrapper: BioSdkWrapper,
        matchParams: MatchParams,
    ) {
        for (batch in channel) {
            val matchResults =
                match(samples, batch, matchParams.flowType, bioSdkWrapper, bioSdk = matchParams.fingerprintSDK!!)
                    .fold(MatchResultSet<FingerprintMatchResult.Item>()) { acc, item ->
                        acc.add(FingerprintMatchResult.Item(item.id, item.score))
                    }
            resultSet.addAll(matchResults)
        }
    }

    private suspend fun match(
        probes: List<CaptureSample>,
        candidates: List<Identity>,
        flowType: FlowType,
        bioSdkWrapper: BioSdkWrapper,
        bioSdk: FingerprintConfiguration.BioSdk,
    ) = bioSdkWrapper.match(
        CaptureIdentity(Modality.FINGERPRINT, probes),
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
}
