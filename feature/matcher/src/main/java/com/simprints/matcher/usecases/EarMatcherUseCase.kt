package com.simprints.matcher.usecases

import com.simprints.core.DispatcherBG
import com.simprints.ear.infra.basebiosdk.matching.EarIdentity
import com.simprints.ear.infra.basebiosdk.matching.EarMatcher
import com.simprints.ear.infra.basebiosdk.matching.EarSample
import com.simprints.ear.infra.biosdkresolver.ResolveEarBioSdkUseCase
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.Simber
import com.simprints.matcher.EarMatchResult
import com.simprints.matcher.MatchParams
import com.simprints.matcher.usecases.MatcherUseCase.MatcherState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import javax.inject.Inject

internal class EarMatcherUseCase @Inject constructor(
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val resolveFaceBioSdk: ResolveEarBioSdkUseCase,
    private val createRanges: CreateRangesUseCase,
    @DispatcherBG private val dispatcher: CoroutineDispatcher,
) : MatcherUseCase {
    private lateinit var earMatcher: EarMatcher
    override val crashReportTag = LoggingConstants.CrashReportTag.EAR_MATCHING

    override suspend operator fun invoke(
        matchParams: MatchParams,
        project: Project,
    ): Flow<MatcherState> = channelFlow {
        Simber.i("Initialising matcher", tag = crashReportTag)
        earMatcher = resolveFaceBioSdk().matcher
        if (matchParams.probeEarSamples.isEmpty()) {
            send(MatcherState.Success(emptyList(), 0, earMatcher.matcherName))
            return@channelFlow
        }
        val samples = mapSamples(matchParams.probeEarSamples)
        val queryWithSupportedFormat = matchParams.queryForCandidates.copy(
            earSampleFormat = earMatcher.supportedTemplateFormat,
        )
        val expectedCandidates = enrolmentRecordRepository.count(
            queryWithSupportedFormat,
            dataSource = matchParams.biometricDataSource,
        )
        if (expectedCandidates == 0) {
            send(MatcherState.Success(emptyList(), 0, earMatcher.matcherName))
            return@channelFlow
        }

        Simber.i("Matching candidates", tag = crashReportTag)
        send(MatcherState.LoadingStarted(expectedCandidates))
        // When using local DB loadedCandidates = expectedCandidates
        // However, when using CommCare as data source, loadedCandidates < expectedCandidates
        // as it's count function does not take into account filtering criteria
        var loadedCandidates = 0
        val resultItems = coroutineScope {
            createRanges(expectedCandidates)
                .map { range ->
                    async(dispatcher) {
                        val batchCandidates = getCandidates(
                            queryWithSupportedFormat,
                            range,
                            project = project,
                            dataSource = matchParams.biometricDataSource,
                        ) {
                            // When a candidate is loaded
                            loadedCandidates++
                            trySend(MatcherState.CandidateLoaded)
                        }
                        match(batchCandidates, samples)
                    }
                }.awaitAll()
                .reduce { acc, subSet -> acc.addAll(subSet) }
                .toList()
        }

        Simber.i("Matched $loadedCandidates candidates", tag = crashReportTag)

        send(MatcherState.Success(resultItems, loadedCandidates, earMatcher.matcherName))
    }

    private fun mapSamples(probes: List<MatchParams.EarSample>) = probes.map { EarSample(it.earId, it.template) }

    private suspend fun getCandidates(
        query: SubjectQuery,
        range: IntRange,
        dataSource: BiometricDataSource = BiometricDataSource.Simprints,
        project: Project,
        onCandidateLoaded: () -> Unit,
    ) = enrolmentRecordRepository
        .loadEarIdentities(query, range, dataSource, project, onCandidateLoaded)
        .map {
            EarIdentity(
                it.subjectId,
                it.ears.map { ear -> EarSample(ear.id, ear.template) },
            )
        }

    private suspend fun match(
        batchCandidates: List<EarIdentity>,
        samples: List<EarSample>,
    ) = batchCandidates.fold(MatchResultSet<EarMatchResult.Item>()) { acc, item ->
        acc.add(
            EarMatchResult.Item(
                item.subjectId,
                earMatcher.getHighestComparisonScoreForCandidate(samples, item),
            ),
        )
    }
}
