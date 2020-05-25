package com.simprints.face.match

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.extentions.concurrentMap
import com.simprints.face.controllers.core.flow.Action
import com.simprints.face.controllers.core.flow.MasterFlowManager
import com.simprints.face.controllers.core.repository.FaceDbManager
import com.simprints.face.data.db.person.FaceIdentity
import com.simprints.face.data.db.person.FaceSample
import com.simprints.face.data.moduleapi.face.requests.FaceMatchRequest
import com.simprints.face.data.moduleapi.face.responses.entities.FaceMatchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import java.io.Serializable

class FaceMatchViewModel(
    private val masterFlowManager: MasterFlowManager,
    private val faceDbManager: FaceDbManager,
    private val faceMatcher: FaceMatcher
) : ViewModel() {
    private lateinit var probeFaceSamples: List<FaceSample>
    private lateinit var queryForCandidates: Serializable

    val matchState: MutableLiveData<MatchState> = MutableLiveData()
    val sortedResults: MutableLiveData<LiveDataEventWithContent<List<FaceMatchResult>>> =
        MutableLiveData()

    fun setupMatch(faceRequest: FaceMatchRequest) = viewModelScope.launch {
        probeFaceSamples = faceRequest.probeFaceSamples
        queryForCandidates = faceRequest.queryForCandidates

        when (masterFlowManager.getCurrentAction()) {
            Action.IDENTIFY -> {
                matchState.value = MatchState.NOT_STARTED_IDENTIFY
            }
            Action.VERIFY -> {
                matchState.value = MatchState.NOT_STARTED_VERIFY
            }
            else -> {
                matchState.value = MatchState.ERROR
                return@launch
            }
        }

        val candidates = loadCandidates()
        val results = matchCandidates(candidates)
        sendSortedResults(results)
    }

    private suspend fun loadCandidates(): Flow<FaceIdentity> {
        matchState.value = MatchState.LOADING_CANDIDATES
        return faceDbManager.loadPeople(queryForCandidates)
    }

    private suspend fun matchCandidates(candidates: Flow<FaceIdentity>): Flow<FaceMatchResult> {
        matchState.value = MatchState.MATCHING
        return getConcurrentMatchResultsForCandidates(candidates)
    }

    private suspend fun sendSortedResults(results: Flow<FaceMatchResult>) {
        matchState.value = MatchState.FINISHED
        sortedResults.send(results.toList().sortedByDescending { it.confidence })
    }

    /**
     * Run in a concurrent map, making the processing much faster
     */
    private suspend fun getConcurrentMatchResultsForCandidates(candidates: Flow<FaceIdentity>) =
        candidates.concurrentMap(Dispatchers.Default) { candidate ->
            FaceMatchResult(
                candidate.faceId,
                faceMatcher.getHighestComparisonScoreForCandidate(probeFaceSamples, candidate)
            )
        }

    enum class MatchState {
        NOT_STARTED_IDENTIFY, NOT_STARTED_VERIFY, LOADING_CANDIDATES, MATCHING, FINISHED, ERROR
    }
}
