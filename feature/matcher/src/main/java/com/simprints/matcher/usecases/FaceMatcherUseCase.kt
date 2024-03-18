package com.simprints.matcher.usecases

import com.simprints.infra.enrolment.records.store.EnrolmentRecordRepository
import com.simprints.core.DispatcherBG
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
import com.simprints.infra.facebiosdk.matching.FaceIdentity
import com.simprints.infra.facebiosdk.matching.FaceMatcher
import com.simprints.infra.facebiosdk.matching.FaceSample
import com.simprints.infra.logging.LoggingConstants
import com.simprints.matcher.FaceMatchResult
import com.simprints.matcher.MatchParams
import com.simprints.matcher.MatchResultItem
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
    override suspend fun matcherName ()= faceMatcher.matcherName

    override suspend operator fun invoke(
        matchParams: MatchParams,
        onLoadingCandidates: (tag: String) -> Unit,
    ): Pair<List<MatchResultItem>, Int> = coroutineScope {
        if (matchParams.probeFaceSamples.isEmpty()) {
            return@coroutineScope Pair(emptyList(), 0)
        }
        val samples = mapSamples(matchParams.probeFaceSamples)
        val totalCandidates = enrolmentRecordRepository.count(matchParams.queryForCandidates)
        if (totalCandidates == 0) {
            return@coroutineScope Pair(emptyList(), 0)
        }

        onLoadingCandidates(crashReportTag)
        createRanges(totalCandidates)
            .map { range ->
                async(dispatcher) {
                    val batchCandidates = getCandidates(matchParams.queryForCandidates, range)
                    match(batchCandidates, samples)
                }
            }
            .awaitAll()
            .reduce { acc, subSet -> acc.addAll(subSet) }
            .toList() to totalCandidates
    }

    private fun mapSamples(probes: List<MatchParams.FaceSample>) = probes
        .map { FaceSample(it.faceId, it.template) }

    private suspend fun getCandidates(query: SubjectQuery, range: IntRange) = enrolmentRecordRepository
        .loadFaceIdentities(query, range)
        .map {
            FaceIdentity(
                it.personId,
                it.faces.map { face -> FaceSample(face.id, face.template) }
            )
        }

    private suspend fun match(
        batchCandidates: List<FaceIdentity>,
        samples: List<FaceSample>,
    ) = batchCandidates.fold(MatchResultSet<FaceMatchResult.Item>()) { acc, item ->
        acc.add(
            FaceMatchResult.Item(
                item.faceId,
                faceMatcher.getHighestComparisonScoreForCandidate(samples, item)
            )
        )
    }

}
