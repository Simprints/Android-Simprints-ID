package com.simprints.matcher.usecases

import com.simprints.core.DispatcherBG
import com.simprints.core.DispatcherIO
import com.simprints.face.infra.basebiosdk.matching.FaceIdentity
import com.simprints.face.infra.basebiosdk.matching.FaceMatcher
import com.simprints.face.infra.basebiosdk.matching.FaceSample
import com.simprints.face.infra.biosdkresolver.ResolveFaceBioSdkUseCase
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.Simber
import com.simprints.matcher.FaceMatchResult
import com.simprints.matcher.MatchParams
import com.simprints.matcher.usecases.MatcherUseCase.MatcherState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

internal class FaceMatcherUseCase @Inject constructor(
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val resolveFaceBioSdk: ResolveFaceBioSdkUseCase,
    private val createRanges: CreateRangesUseCase,
    @DispatcherBG private val dispatcherBG: CoroutineDispatcher,
    @DispatcherIO private val dispatcherIO: CoroutineDispatcher,
) : MatcherUseCase {
    override val crashReportTag = LoggingConstants.CrashReportTag.FACE_MATCHING

    override suspend operator fun invoke(
        matchParams: MatchParams,
        project: Project,
    ): Flow<MatcherState> = channelFlow {
        Simber.i("Initialising matcher", tag = crashReportTag)
        val bioSdk = resolveFaceBioSdk()

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
        var loadedCandidates = 0
        val numWorkers = Runtime.getRuntime().availableProcessors()
        val channel = Channel<List<FaceIdentity>>(numWorkers)
        val resultSet = MatchResultSet<FaceMatchResult.Item>()
        val mutex = Mutex()
        // Producer(s) - single or multiple
        val producer = launch(dispatcherIO) {
            for (range in createRanges(expectedCandidates)) {
                val batch = getCandidates(queryWithSupportedFormat, range, matchParams.biometricDataSource, project) {
                    trySend(MatcherState.CandidateLoaded)
                    loadedCandidates++
                }

                channel.send(batch)
            }
            channel.close()
        }

        val consumers = List(numWorkers) {
            launch(dispatcherBG) {
                for (batchCandidates in channel) {
                    val subSet = bioSdk.createMatcher(samples).use { match(it, batchCandidates) }
                    // Merge results safely
                    mutex.withLock {
                        resultSet.addAll(subSet)
                    }
                }
            }
        }
        // Wait for all
        producer.join()
        consumers.joinAll()

        send(MatcherState.Success(resultSet.toList(), loadedCandidates, bioSdk.matcherName))
    }.flowOn(dispatcherBG)

    private fun mapSamples(probes: List<MatchParams.FaceSample>) = probes.map { FaceSample(it.faceId, it.template) }

    private suspend fun getCandidates(
        query: SubjectQuery,
        range: IntRange,
        dataSource: BiometricDataSource = BiometricDataSource.Simprints,
        project: Project,
        onCandidateLoaded: () -> Unit,
    ) = enrolmentRecordRepository
        .loadFaceIdentities(query, range, dataSource, project, onCandidateLoaded)
        .map {
            FaceIdentity(
                it.subjectId,
                it.faces.map { face -> FaceSample(face.id, face.template) },
            )
        }

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
}
