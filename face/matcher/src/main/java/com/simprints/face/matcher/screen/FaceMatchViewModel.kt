package com.simprints.face.matcher.screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.time.TimeHelper
import com.simprints.face.matcher.FaceMatchParams
import com.simprints.face.matcher.FaceMatchResult
import com.simprints.face.matcher.usecases.LoadPeopleFaceIdentityUseCase
import com.simprints.face.matcher.usecases.SaveMatchEventUseCase
import com.simprints.infra.facebiosdk.matching.FaceIdentity
import com.simprints.infra.facebiosdk.matching.FaceMatcher
import com.simprints.infra.facebiosdk.matching.FaceSample
import com.simprints.infra.logging.LoggingConstants.CrashReportTag
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import java.io.Serializable
import javax.inject.Inject


@HiltViewModel
internal class FaceMatchViewModel @Inject constructor(
    private val loadPeopleFaceIdentities: LoadPeopleFaceIdentityUseCase,
    private val faceMatcher: FaceMatcher,
    private val saveMatchEvent: SaveMatchEventUseCase,
    private val timeHelper: TimeHelper,
) : ViewModel() {

    val matchState: LiveData<MatchState>
        get() = _matchState
    private val _matchState = MutableLiveData<MatchState>(MatchState.NotStarted)

    val faceMatchResponse: LiveData<LiveDataEventWithContent<FaceMatchResult>>
        get() = _faceMatchResponse
    private val _faceMatchResponse = MutableLiveData<LiveDataEventWithContent<FaceMatchResult>>()

    fun setupMatch(faceRequest: FaceMatchParams) = viewModelScope.launch {
        val startTime = timeHelper.now()
        val samples = faceRequest.probeFaceSamples.map { FaceSample(it.faceId, it.template) }
        val candidates = loadCandidates(faceRequest.queryForCandidates)
        val results = matchCandidates(samples, candidates)
        val sortedResults = results.toList().sortedByDescending { it.confidence }
        val maxFilteredResults = sortedResults.take(returnCount)

        val endTime = timeHelper.now()

        saveMatchEvent(
            startTime,
            endTime,
            faceRequest,
            sortedResults.size,
            faceMatcher.matcherName,
            maxFilteredResults
        )

        sendFaceMatchResponse(sortedResults.size, maxFilteredResults)
    }

    private suspend fun loadCandidates(queryForCandidates: Serializable): Flow<FaceIdentity> {
        Simber.tag(CrashReportTag.FACE_MATCHING.name).i("Loading candidates")

        println(MatchState.LoadingCandidates)
        _matchState.postValue(MatchState.LoadingCandidates)
        return loadPeopleFaceIdentities(queryForCandidates)
    }

    private suspend fun matchCandidates(
        probeFaceSamples: List<FaceSample>,
        candidates: Flow<FaceIdentity>
    ): Flow<FaceMatchResult.Item> {
        Simber.tag(CrashReportTag.FACE_MATCHING.name).i("Matching probe against candidates")

        _matchState.postValue(MatchState.Matching)
        return getConcurrentMatchResultsForCandidates(probeFaceSamples, candidates)
    }

    private suspend fun getConcurrentMatchResultsForCandidates(
        probeFaceSamples: List<FaceSample>,
        candidates: Flow<FaceIdentity>
    ) = candidates.map { candidate ->
        FaceMatchResult.Item(
            candidate.faceId,
            faceMatcher.getHighestComparisonScoreForCandidate(probeFaceSamples, candidate)
        )
    }

    private fun sendFaceMatchResponse(candidatesMatched: Int, results: List<FaceMatchResult.Item>) {
        val veryGoodMatches = results.count { veryGoodMatchThreshold <= it.confidence }
        val goodMatches =
            results.count { goodMatchThreshold <= it.confidence && it.confidence < veryGoodMatchThreshold }
        val fairMatches =
            results.count { fairMatchThreshold <= it.confidence && it.confidence < goodMatchThreshold }

        _matchState.postValue(MatchState.Finished(
            candidatesMatched,
            results.size,
            veryGoodMatches,
            goodMatches,
            fairMatches
        ))
        _faceMatchResponse.send(FaceMatchResult(results))
    }

    sealed class MatchState {
        data object NotStarted : MatchState()
        data object LoadingCandidates : MatchState()
        data object Matching : MatchState()
        data class Finished(
            val candidatesMatched: Int,
            val returnSize: Int,
            val veryGoodMatches: Int,
            val goodMatches: Int,
            val fairMatches: Int
        ) : MatchState()
    }

    companion object {
        const val returnCount = 10
        const val veryGoodMatchThreshold = 50.0
        const val goodMatchThreshold = 35.0
        const val fairMatchThreshold = 20.0
        const val matchingEndWaitTimeInMillis = 1000L
    }
}
