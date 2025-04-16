package com.simprints.matcher.usecases

import com.simprints.core.DispatcherBG
import com.simprints.core.DispatcherIO
import com.simprints.face.infra.basebiosdk.matching.FaceIdentity
import com.simprints.face.infra.basebiosdk.matching.FaceMatcher
import com.simprints.face.infra.basebiosdk.matching.FaceSample
import com.simprints.face.infra.biosdkresolver.FaceBioSDK
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
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class FaceMatcherUseCase @Inject constructor(
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val resolveFaceBioSdk: ResolveFaceBioSdkUseCase,
    private val createRanges: CreateRangesUseCase,
    @DispatcherIO private val dispatcherIO: CoroutineDispatcher,
    @DispatcherBG private val dispatcherBG: CoroutineDispatcher,
) : MatcherUseCase {
    override val crashReportTag = LoggingConstants.CrashReportTag.FACE_MATCHING

    // When using local DB loadedCandidates = expectedCandidates
    // However, when using CommCare as data source, loadedCandidates < expectedCandidates
    // as it's count function does not take into account filtering criteria
    // This var is not thread safe
    var loadedCandidates = 0

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
        loadedCandidates = 0
        Simber.i("Matching candidates", tag = crashReportTag)
        send(MatcherState.LoadingStarted(expectedCandidates))

        val numConsumers = Runtime.getRuntime().availableProcessors() - 1 // Leave one for other tasks
        val channel = Channel<List<FaceIdentity>>(capacity = numConsumers)
        val resultSet = MatchResultSet<FaceMatchResult.Item>()
        // Start Producer in IO thread
        val producerJob = launch(dispatcherIO) {
            produceCandidates(
                channel,
                expectedCandidates,
                queryWithSupportedFormat,
                project,
                matchParams,
                this@channelFlow,
            )
        }
        // Start Consumers in BG thread
        val consumerJobs = List(numConsumers) {
            launch(dispatcherBG) {
                consumeAndMatch(channel, samples, resultSet, bioSdk)
            }
        }
        // Wait for all to complete
        producerJob.join()
        consumerJobs.forEach { it.join() }
        send(MatcherState.Success(resultSet.toList(), loadedCandidates, bioSdk.matcherName))
    }

    suspend fun produceCandidates(
        channel: SendChannel<List<FaceIdentity>>,
        expectedCandidates: Int,
        queryWithSupportedFormat: SubjectQuery,
        project: Project,
        matchParams: MatchParams,
        progressTracker: SendChannel<MatcherState>,
    ) {
        for (range in createRanges(expectedCandidates)) {
            val batch = getCandidates(
                queryWithSupportedFormat,
                range,
                project = project,
                dataSource = matchParams.biometricDataSource,
            ) {
                loadedCandidates++
                progressTracker.trySend(MatcherState.CandidateLoaded)
            }
            channel.send(batch) // Send batch to consumers
        }
        channel.close() // Close channel when done
    }

    suspend fun consumeAndMatch(
        channel: ReceiveChannel<List<FaceIdentity>>,
        samples: List<FaceSample>,
        resultSet: MatchResultSet<FaceMatchResult.Item>,
        bioSdk: FaceBioSDK,
    ) {
        for (batch in channel) {
            val results = bioSdk.createMatcher(samples).use { matcher ->
                match(matcher, batch)
            }
            resultSet.addAll(results)
        }
    }

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
