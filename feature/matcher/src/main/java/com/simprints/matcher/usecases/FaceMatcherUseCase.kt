package com.simprints.matcher.usecases

import com.simprints.core.DispatcherBG
import com.simprints.core.domain.modality.Modality
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.core.domain.sample.Identity
import com.simprints.face.infra.basebiosdk.matching.FaceMatcher
import com.simprints.face.infra.biosdkresolver.FaceBioSDK
import com.simprints.face.infra.biosdkresolver.ResolveFaceBioSdkUseCase
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.Simber
import com.simprints.matcher.MatchParams
import com.simprints.matcher.MatchResultItem
import com.simprints.matcher.usecases.MatcherUseCase.MatcherState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

internal class FaceMatcherUseCase @Inject constructor(
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val resolveFaceBioSdk: ResolveFaceBioSdkUseCase,
    private val createRanges: CreateRangesUseCase,
    @DispatcherBG private val dispatcherBG: CoroutineDispatcher,
) : MatcherUseCase {
    override val crashReportTag = LoggingConstants.CrashReportTag.FACE_MATCHING

    override suspend operator fun invoke(
        matchParams: MatchParams,
        project: Project,
    ): Flow<MatcherState> = channelFlow {
        Simber.i("Initialising matcher", tag = crashReportTag)
        if (matchParams.sdkType !is FaceConfiguration.BioSdk) {
            Simber.w("Face SDK was not provided", tag = crashReportTag)
            send(MatcherState.Success(emptyList(), 0, ""))
            return@channelFlow
        }
        val bioSdk = resolveFaceBioSdk(matchParams.sdkType)

        if (matchParams.probeSamples.isEmpty()) {
            send(MatcherState.Success(emptyList(), 0, bioSdk.matcherName()))
            return@channelFlow
        }

        val queryWithFormatAndModality = matchParams.queryForCandidates.copy(
            sampleFormat = bioSdk.templateFormat(),
            modality = Modality.FACE,
        )
        val expectedCandidates = enrolmentRecordRepository.count(
            queryWithFormatAndModality,
            dataSource = matchParams.biometricDataSource,
        )
        if (expectedCandidates == 0) {
            send(MatcherState.Success(emptyList(), 0, bioSdk.matcherName()))
            return@channelFlow
        }

        Simber.i("Matching candidates", tag = crashReportTag)
        send(MatcherState.LoadingStarted(expectedCandidates))

        // When using local DB loadedCandidates = expectedCandidates
        // However, when using CommCare as data source, loadedCandidates < expectedCandidates
        // as it's count function does not take into account filtering criteria
        val loadedCandidates = AtomicInteger(0)
        val ranges = createRanges(expectedCandidates)
        val resultSet = MatchResultSet<MatchResultItem>()
        val candidatesChannel = enrolmentRecordRepository
            .loadIdentities(
                query = queryWithFormatAndModality,
                ranges = ranges,
                dataSource = matchParams.biometricDataSource,
                project = project,
                scope = this,
            ) {
                loadedCandidates.incrementAndGet()
                this@channelFlow.send(MatcherState.CandidateLoaded)
            }

        consumeAndMatch(candidatesChannel, matchParams.probeSamples, resultSet, bioSdk)
        send(MatcherState.Success(resultSet.toList(), loadedCandidates.get(), bioSdk.matcherName()))
    }.flowOn(dispatcherBG)

    suspend fun consumeAndMatch(
        candidatesChannel: ReceiveChannel<List<Identity>>,
        samples: List<CaptureSample>,
        resultSet: MatchResultSet<MatchResultItem>,
        bioSdk: FaceBioSDK,
    ) {
        for (batch in candidatesChannel) {
            val results = bioSdk.createMatcher(samples).use { matcher ->
                match(matcher, batch)
            }
            resultSet.addAll(results)
        }
    }

    private suspend fun match(
        matcher: FaceMatcher,
        batchCandidates: List<Identity>,
    ) = batchCandidates.fold(MatchResultSet<MatchResultItem>()) { acc, candidate ->
        acc.add(
            MatchResultItem(
                candidate.subjectId,
                matcher.getHighestComparisonScoreForCandidate(candidate),
            ),
        )
    }
}
