package com.simprints.matcher.usecases

import com.simprints.core.DispatcherBG
import com.simprints.infra.enrolment.records.store.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.store.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
import com.simprints.infra.facebiosdk.matching.FaceIdentity
import com.simprints.infra.facebiosdk.matching.FaceMatcher
import com.simprints.infra.facebiosdk.matching.FaceSample
import com.simprints.infra.logging.LoggingConstants
import com.simprints.matcher.FaceMatchResult
import com.simprints.matcher.MatchParams
import com.simprints.matcher.usecases.MatcherUseCase.MatcherResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

internal class FaceMatcherUseCase @Inject constructor(
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val faceMatcher: FaceMatcher,
    private val createRanges: CreateRangesUseCase,
    @DispatcherBG private val dispatcher: CoroutineDispatcher,
) : MatcherUseCase {

    override val crashReportTag = LoggingConstants.CrashReportTag.FACE_MATCHING.name

    override suspend operator fun invoke(
        matchParams: MatchParams,
        onLoadingCandidates: (tag: String) -> Unit,
    ): MatcherResult = coroutineScope {
        if (matchParams.probeFaceSamples.isEmpty()) {
            return@coroutineScope MatcherResult(emptyList(), 0, faceMatcher.matcherName)
        }
        val samples = mapSamples(matchParams.probeFaceSamples)
        val totalCandidates = enrolmentRecordRepository.count(matchParams.queryForCandidates, dataSource = matchParams.biometricDataSource)
        if (totalCandidates == 0) {
            return@coroutineScope MatcherResult(emptyList(), 0, faceMatcher.matcherName)
        }

        onLoadingCandidates(crashReportTag)
        val resultItems = createRanges(totalCandidates)
            .map { range ->
                async(dispatcher) {
                    val batchCandidates = getCandidates(matchParams.queryForCandidates, range, dataSource = matchParams.biometricDataSource)
                    match(batchCandidates, samples)
                }
            }
            .awaitAll()
            .reduce { acc, subSet -> acc.addAll(subSet) }
            .toList()
        MatcherResult(resultItems, totalCandidates, faceMatcher.matcherName)
    }

    private fun mapSamples(probes: List<MatchParams.FaceSample>) = probes
        .map { FaceSample(it.faceId, it.template) }

    private suspend fun getCandidates(
        query: SubjectQuery,
        range: IntRange,
        dataSource: BiometricDataSource = BiometricDataSource.SIMPRINTS,
    ) = enrolmentRecordRepository
        .loadFaceIdentities(query, range, dataSource)
        .map {
            FaceIdentity(
                it.subjectId,
                it.faces.map { face -> FaceSample(face.id, face.template) }
            )
        }

    private suspend fun match(
        batchCandidates: List<FaceIdentity>,
        samples: List<FaceSample>,
    ) = batchCandidates.fold(MatchResultSet<FaceMatchResult.Item>()) { acc, item ->
        acc.add(
            FaceMatchResult.Item(
                item.subjectId,
                faceMatcher.getHighestComparisonScoreForCandidate(samples, item)
            )
        )
    }

}
