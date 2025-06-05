package com.simprints.matcher.usecases

import com.simprints.core.DispatcherBG
import com.simprints.face.infra.basebiosdk.matching.FaceIdentity
import com.simprints.face.infra.basebiosdk.matching.FaceMatcher
import com.simprints.face.infra.basebiosdk.matching.FaceSample
import com.simprints.face.infra.biosdkresolver.FaceBioSDK
import com.simprints.face.infra.biosdkresolver.ResolveFaceBioSdkUseCase
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.Simber
import com.simprints.matcher.FaceMatchResult
import com.simprints.matcher.MatchParams
import com.simprints.matcher.usecases.MatcherUseCase.MatcherState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import com.simprints.infra.enrolment.records.repository.domain.models.FaceIdentity as DomainFaceIdentity

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
        if (matchParams.faceSDK == null) {
            Simber.w("Face SDK was not provided", tag = crashReportTag)
            send(MatcherState.Success(emptyList(), 0, ""))
            return@channelFlow
        }
        val bioSdk = resolveFaceBioSdk(matchParams.faceSDK)

        if (matchParams.probeFaceSamples.isEmpty()) {
            send(MatcherState.Success(emptyList(), 0, bioSdk.matcherName))
            return@channelFlow
        }
        val samples = mapSamples(matchParams.probeFaceSamples)
        val queryWithSupportedFormat = matchParams.queryForCandidates.copy(
            faceSampleFormat = bioSdk.templateFormat,
        )
        val expectedCandidates = enrolmentRecordRepository.count(
            queryWithSupportedFormat,
            dataSource = matchParams.biometricDataSource,
        )
        if (expectedCandidates == 0) {
            send(MatcherState.Success(emptyList(), 0, bioSdk.matcherName))
            return@channelFlow
        }

        Simber.i("Matching candidates", tag = crashReportTag)
        send(MatcherState.LoadingStarted(expectedCandidates))

        // When using local DB loadedCandidates = expectedCandidates
        // However, when using CommCare as data source, loadedCandidates < expectedCandidates
        // as it's count function does not take into account filtering criteria
        val loadedCandidates = AtomicInteger(0)
        val ranges = createRanges(expectedCandidates)
        val resultSet = MatchResultSet<FaceMatchResult.Item>()
        val candidatesChannel = enrolmentRecordRepository
            .loadFaceIdentities(
                query = queryWithSupportedFormat,
                ranges = ranges,
                dataSource = matchParams.biometricDataSource,
                project = project,
                scope = this,
                onCandidateLoaded = {
                    loadedCandidates.incrementAndGet()
                    this@channelFlow.send(MatcherState.CandidateLoaded)
                },
            )
        consumeAndMatch(candidatesChannel, samples, resultSet, bioSdk)
        send(MatcherState.Success(resultSet.toList(), loadedCandidates.get(), bioSdk.matcherName))
    }.flowOn(dispatcherBG)

    suspend fun consumeAndMatch(
        candidatesChannel: ReceiveChannel<List<DomainFaceIdentity>>,
        samples: List<FaceSample>,
        resultSet: MatchResultSet<FaceMatchResult.Item>,
        bioSdk: FaceBioSDK,
    ) {
        for (batch in candidatesChannel) {
            val results = bioSdk.createMatcher(samples).use { matcher ->
                match(matcher, batch.mapToFaceIdentities())
            }
            resultSet.addAll(results)
        }
    }

    private fun mapSamples(probes: List<MatchParams.FaceSample>) = probes.map { FaceSample(it.faceId, it.template) }

    private suspend fun match(
        matcher: FaceMatcher,
        batchCandidates: List<FaceIdentity>,
    ) = batchCandidates.fold(MatchResultSet<FaceMatchResult.Item>()) { acc, candidate ->
        acc.add(
            FaceMatchResult.Item(
                candidate.subjectId,
                matcher.getHighestComparisonScoreForCandidate(candidate),
            ),
        )
    }

    private fun List<DomainFaceIdentity>.mapToFaceIdentities(): List<FaceIdentity> = map {
        FaceIdentity(
            it.subjectId,
            it.faces.map {
                FaceSample(
                    it.referenceId,
                    it.template,
                )
            },
        )
    }
}
