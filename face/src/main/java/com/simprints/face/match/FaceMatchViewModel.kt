package com.simprints.face.match

import androidx.lifecycle.ViewModel
import com.simprints.face.data.moduleapi.face.requests.FaceMatchRequest
import com.simprints.face.data.moduleapi.face.requests.FaceRequest

class FaceMatchViewModel : ViewModel() {
    fun setupMatch(faceRequest: FaceRequest) {
        when (faceRequest) {
            is FaceMatchRequest -> {
                faceRequest.probeFaceSamples
                faceRequest.queryForCandidates
            }
        }
    }
}
