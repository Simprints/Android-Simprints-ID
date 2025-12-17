package com.simprints.infra.matching.usecase

import com.simprints.core.DispatcherBG
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.capture.BiometricReferenceCapture
import com.simprints.core.domain.reference.CandidateRecord
import com.simprints.core.tools.time.TimeHelper
import com.simprints.fingerprint.infra.biosdk.BioSdkWrapper
import com.simprints.fingerprint.infra.biosdk.ResolveBioSdkWrapperUseCase
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration.FingerComparisonStrategy.CROSS_FINGER_USING_MEAN_OF_MAX
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.CandidateRecordBatch
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.Simber
import com.simprints.infra.matching.MatchBatchInfo
import com.simprints.infra.matching.MatchParams
import com.simprints.infra.matching.usecase.MatcherUseCase.MatcherState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class FingerprintMatcherUseCase @Inject constructor(
    private val timeHelper: TimeHelper,
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
        if (matchParams.bioSdk !is FingerprintConfiguration.BioSdk) {
            Simber.w(
                message = "Fingerprint SDK was not provided",
                t = IllegalArgumentException("Fingerprint SDK was not provided"),
                tag = crashReportTag,
            )
            send(MatcherState.Success(emptyList(), emptyList(), 0, ""))
            return@channelFlow
        }
        val bioSdkWrapper = resolveBioSdkWrapper(matchParams.bioSdk)
        if (matchParams.probeReference.templates.isEmpty()) {
            send(MatcherState.Success(emptyList(), emptyList(), 0, bioSdkWrapper.matcherName))
            return@channelFlow
        }

        // Only candidates with supported template format are considered
        val queryWithSupportedFormat =
            matchParams.queryForCandidates.copy(
                format = bioSdkWrapper.supportedTemplateFormat,
            )
        val expectedCandidates = enrolmentRecordRepository.count(queryWithSupportedFormat, dataSource = matchParams.biometricDataSource)
        if (expectedCandidates == 0) {
            send(MatcherState.Success(emptyList(), emptyList(), 0, bioSdkWrapper.matcherName))
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
        val channel = enrolmentRecordRepository.loadCandidateRecords(
            query = queryWithSupportedFormat,
            ranges = ranges,
            dataSource = matchParams.biometricDataSource,
            scope = this,
            project = project,
        ) {
            loadedCandidates.incrementAndGet()
            this@channelFlow.send(MatcherState.CandidateLoaded)
        }

        val resultSet = MatchResultSet()

        val batchInfo = consumeAndMatch(
            channel = channel,
            probeReference = matchParams.probeReference,
            resultSet = resultSet,
            bioSdk = matchParams.bioSdk,
            bioSdkWrapper = bioSdkWrapper,
            flowType = matchParams.flowType,
        )

        Simber.i("Matched $loadedCandidates candidates", tag = crashReportTag)
        send(MatcherState.Success(resultSet.toList(), batchInfo, loadedCandidates.get(), bioSdkWrapper.matcherName))
    }.flowOn(dispatcherBG)

    private suspend fun consumeAndMatch(
        channel: ReceiveChannel<CandidateRecordBatch>,
        probeReference: BiometricReferenceCapture,
        resultSet: MatchResultSet,
        bioSdk: FingerprintConfiguration.BioSdk,
        bioSdkWrapper: BioSdkWrapper,
        flowType: FlowType,
    ): List<MatchBatchInfo> {
        val matchBatches = mutableListOf<MatchBatchInfo>()
        for (batch in channel) {
            val comparingStartTime = timeHelper.now()
            val matchResults =
                match(
                    probeReference = probeReference,
                    candidates = batch.identities,
                    flowType = flowType,
                    bioSdkWrapper = bioSdkWrapper,
                    bioSdk = bioSdk,
                ).fold(MatchResultSet()) { acc, item -> acc.add(item) }
            resultSet.addAll(matchResults)
            val comparingEndTime = timeHelper.now()
            matchBatches.add(
                MatchBatchInfo(
                    batch.loadingStartTime,
                    batch.loadingEndTime,
                    comparingStartTime,
                    comparingEndTime,
                    batch.identities.size,
                ),
            )
        }
        return matchBatches
    }

    private suspend fun match(
        probeReference: BiometricReferenceCapture,
        candidates: List<CandidateRecord>,
        flowType: FlowType,
        bioSdkWrapper: BioSdkWrapper,
        bioSdk: FingerprintConfiguration.BioSdk,
    ) = bioSdkWrapper.match(
        probeReference,
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
