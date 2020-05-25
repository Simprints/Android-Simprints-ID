package com.simprints.face.match

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.coroutines.DefaultDispatcherProvider
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.core.tools.extentions.concurrentMap
import com.simprints.face.controllers.core.flow.Action
import com.simprints.face.controllers.core.flow.MasterFlowManager
import com.simprints.face.controllers.core.repository.FaceDbManager
import com.simprints.face.data.db.person.FaceIdentity
import com.simprints.face.data.db.person.FaceSample
import com.simprints.face.data.moduleapi.face.requests.FaceMatchRequest
import com.simprints.face.data.moduleapi.face.responses.FaceMatchResponse
import com.simprints.face.data.moduleapi.face.responses.entities.FaceMatchResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import java.io.Serializable

class FaceMatchViewModel(
    private val masterFlowManager: MasterFlowManager,
    private val faceDbManager: FaceDbManager,
    private val faceMatcher: FaceMatcher,
    private val dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider()
) : ViewModel() {
    private lateinit var probeFaceSamples: List<FaceSample>
    private lateinit var queryForCandidates: Serializable

    val matchState: MutableLiveData<MatchState> = MutableLiveData(MatchState.NotStarted)
    val faceMatchResponse: MutableLiveData<LiveDataEventWithContent<FaceMatchResponse>> =
        MutableLiveData()
    private val returnCount = 10
    private val veryGoodMatchThreshold = 50.0
    private val goodMatchThreshold = 35.0
    private val fairMatchThreshold = 20.0
    private val matchingEndWaitTimeInMillis = 1000L

    private var candidatesMatched = 0

    fun setupMatch(faceRequest: FaceMatchRequest) = viewModelScope.launch {
        probeFaceSamples = faceRequest.probeFaceSamples
        queryForCandidates = faceRequest.queryForCandidates

        if (masterFlowManager.getCurrentAction() == Action.ENROL) {
            matchState.value = MatchState.Error
            return@launch
        }

        val candidates = loadCandidates()
        val results = matchCandidates(candidates)
        val sortedResults = getNSortedResult(results)
        sendFaceMatchResponse(sortedResults)
    }

    private suspend fun loadCandidates(): Flow<FaceIdentity> {
        matchState.value = MatchState.LoadingCandidates
        return faceDbManager.loadPeople(queryForCandidates)
    }

    private suspend fun matchCandidates(candidates: Flow<FaceIdentity>): Flow<FaceMatchResult> {
        return getConcurrentMatchResultsForCandidates(candidates)
    }

    /**
     * Run in a concurrent map, making the processing much faster
     */
    private suspend fun getConcurrentMatchResultsForCandidates(candidates: Flow<FaceIdentity>) =
        candidates.concurrentMap(dispatcherProvider.default()) { candidate ->
            matchState.postValue(MatchState.Matching(++candidatesMatched))
            FaceMatchResult(
                candidate.faceId,
                faceMatcher.getHighestComparisonScoreForCandidate(probeFaceSamples, candidate)
            )
        }

    private suspend fun getNSortedResult(results: Flow<FaceMatchResult>): List<FaceMatchResult> =
        results.toList().sortedByDescending { it.confidence }.take(returnCount)

    private suspend fun sendFaceMatchResponse(results: List<FaceMatchResult>) {
        val veryGoodMatches = results.count { (_, score) -> veryGoodMatchThreshold <= score }
        val goodMatches =
            results.count { (_, score) -> goodMatchThreshold <= score && score < veryGoodMatchThreshold }
        val fairMatches =
            results.count { (_, score) -> fairMatchThreshold <= score && score < goodMatchThreshold }

        matchState.value =
            MatchState.Finished(
                returnCount,
                veryGoodMatches,
                goodMatches,
                fairMatches
            )
        val response = FaceMatchResponse(results)

        // Wait a bit so the user can see the results
        delay(matchingEndWaitTimeInMillis)
        faceMatchResponse.send(response)
    }

    sealed class MatchState {
        object NotStarted : MatchState()
        object Error : MatchState()
        object LoadingCandidates : MatchState()
        data class Matching(val candidatesMatched: Int) : MatchState()
        data class Finished(
            val returnSize: Int,
            val veryGoodMatches: Int,
            val goodMatches: Int,
            val fairMatches: Int
        ) :
            MatchState()
    }
}
