package com.simprints.face.match

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.face.controllers.core.flow.Action
import com.simprints.face.controllers.core.flow.MasterFlowManager
import com.simprints.face.controllers.core.repository.FaceDbManager
import com.simprints.face.data.db.person.FaceIdentity
import com.simprints.face.data.db.person.FaceSample
import com.simprints.face.data.moduleapi.face.requests.FaceMatchRequest
import com.simprints.face.data.moduleapi.face.responses.entities.FaceMatchResult
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

        matchState.value = MatchState.LOADING_CANDIDATES
        val candidates = faceDbManager.loadPeople(queryForCandidates)
        matchState.value = MatchState.MATCHING
        val results = getMatchResultsForCandidates(candidates)
        matchState.value = MatchState.FINISHED
        sortedResults.send(results.sortedByDescending { it.confidence })
    }

    private suspend fun getMatchResultsForCandidates(candidates: List<FaceIdentity>) =
        candidates.map { candidate ->
            FaceMatchResult(
                candidate.faceId,
                faceMatcher.getHighestComparisonScoreForCandidate(probeFaceSamples, candidate)
            )
        }

    enum class MatchState {
        NOT_STARTED_IDENTIFY, NOT_STARTED_VERIFY, LOADING_CANDIDATES, MATCHING, FINISHED, ERROR
    }
}
