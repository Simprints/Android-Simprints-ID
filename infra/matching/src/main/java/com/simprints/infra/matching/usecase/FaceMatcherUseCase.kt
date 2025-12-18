package com.simprints.infra.matching.usecase

import com.simprints.core.DispatcherBG
import com.simprints.core.domain.capture.BiometricReferenceCapture
import com.simprints.core.domain.comparison.ComparisonResult
import com.simprints.core.domain.reference.CandidateRecord
import com.simprints.core.tools.time.TimeHelper
import com.simprints.face.infra.basebiosdk.matching.FaceMatcher
import com.simprints.face.infra.biosdkresolver.FaceBioSDK
import com.simprints.face.infra.biosdkresolver.ResolveFaceBioSdkUseCase
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.Project
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

class FaceMatcherUseCase @Inject constructor(
    private val timeHelper: TimeHelper,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val resolveFaceBioSdk: ResolveFaceBioSdkUseCase,
    private val createRanges: CreateRangesUseCase,
    @param:DispatcherBG private val dispatcherBG: CoroutineDispatcher,
) : MatcherUseCase {
    override val crashReportTag = LoggingConstants.CrashReportTag.FACE_MATCHING

    override suspend operator fun invoke(
        matchParams: MatchParams,
        project: Project,
    ): Flow<MatcherState> = channelFlow {
        Simber.i("Initialising matcher", tag = crashReportTag)
        if (matchParams.bioSdk !is FaceConfiguration.BioSdk) {
            Simber.w(
                message = "Face SDK was not provided",
                t = IllegalArgumentException("Face SDK was not provided"),
                tag = crashReportTag,
            )
            send(MatcherState.Success(emptyList(), emptyList(), 0, ""))
            return@channelFlow
        }
        val bioSdk = resolveFaceBioSdk(matchParams.bioSdk)
        if (matchParams.probeReference.templates.isEmpty()) {
            send(MatcherState.Success(emptyList(), emptyList(), 0, bioSdk.matcherName()))
            return@channelFlow
        }

        val queryWithSupportedFormat = matchParams.queryForCandidates.copy(
            format = bioSdk.templateFormat(),
        )
        val expectedCandidates = enrolmentRecordRepository.count(
            queryWithSupportedFormat,
            dataSource = matchParams.biometricDataSource,
        )
        if (expectedCandidates == 0) {
            send(MatcherState.Success(emptyList(), emptyList(), 0, bioSdk.matcherName()))
            return@channelFlow
        }

        Simber.i("Matching candidates", tag = crashReportTag)
        send(MatcherState.LoadingStarted(expectedCandidates))

        // When using local DB loadedCandidates = expectedCandidates
        // However, when using CommCare as data source, loadedCandidates < expectedCandidates
        // as it's count function does not take into account filtering criteria
        val loadedCandidates = AtomicInteger(0)
        val ranges = createRanges(expectedCandidates)
        val resultSet = MatchResultSet()
        val candidatesChannel = enrolmentRecordRepository
            .loadCandidateRecords(
                query = queryWithSupportedFormat,
                ranges = ranges,
                dataSource = matchParams.biometricDataSource,
                project = project,
                scope = this,
            ) {
                loadedCandidates.incrementAndGet()
                this@channelFlow.send(MatcherState.CandidateLoaded)
            }

        val batchInfo = consumeAndMatch(candidatesChannel, matchParams.probeReference, resultSet, bioSdk)
        send(MatcherState.Success(resultSet.toList(), batchInfo, loadedCandidates.get(), bioSdk.matcherName()))
    }.flowOn(dispatcherBG)

    private suspend fun consumeAndMatch(
        candidatesChannel: ReceiveChannel<CandidateRecordBatch>,
        probeReference: BiometricReferenceCapture,
        resultSet: MatchResultSet,
        bioSdk: FaceBioSDK,
    ): List<MatchBatchInfo> {
        val matchBatches = mutableListOf<MatchBatchInfo>()
        for (batch in candidatesChannel) {
            val comparingStartTime = timeHelper.now()
            val results = bioSdk.createMatcher(probeReference).use { matcher ->
                match(matcher, batch.identities)
            }
            resultSet.addAll(results)
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
        matcher: FaceMatcher,
        batchCandidates: List<CandidateRecord>,
    ) = batchCandidates.fold(MatchResultSet()) { acc, candidate ->
        acc.add(
            ComparisonResult(
                candidate.subjectId,
                matcher.getHighestComparisonScoreForCandidate(candidate),
            ),
        )
    }
}
