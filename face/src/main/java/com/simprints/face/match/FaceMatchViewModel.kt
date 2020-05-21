package com.simprints.face.match

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.face.controllers.core.flow.Action
import com.simprints.face.controllers.core.flow.MasterFlowManager
import com.simprints.face.controllers.core.repository.FaceDbManager
import com.simprints.face.data.db.person.FaceSample
import com.simprints.face.data.moduleapi.face.requests.FaceMatchRequest
import java.io.Serializable

class FaceMatchViewModel(
    private val masterFlowManager: MasterFlowManager,
    private val faceDbManager: FaceDbManager
) : ViewModel() {
    private lateinit var probeFaceSamples: List<FaceSample>
    private lateinit var queryForCandidates: Serializable

    val matchState: MutableLiveData<MatchState> = MutableLiveData()

    fun setupMatch(faceRequest: FaceMatchRequest) {
        probeFaceSamples = faceRequest.probeFaceSamples
        queryForCandidates = faceRequest.queryForCandidates

        when (masterFlowManager.getCurrentAction()) {
            Action.IDENTIFY -> {
                matchState.value = MatchState.NOT_STARTED_IDENTIFY
                //TODO: run identification
            }
            Action.VERIFY -> {
                matchState.value = MatchState.NOT_STARTED_VERIFY
                //TODO: run verification
            }
            else -> {
                matchState.value = MatchState.ERROR
                //TODO: return an error
            }
        }
    }

    enum class MatchState {
        NOT_STARTED_IDENTIFY, NOT_STARTED_VERIFY, LOADING_CANDIDATES, MATCHING, FINISHED, ERROR
    }
}
