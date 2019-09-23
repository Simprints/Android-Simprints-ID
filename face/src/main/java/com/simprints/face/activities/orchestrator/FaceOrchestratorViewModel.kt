package com.simprints.face.activities.orchestrator

import androidx.lifecycle.ViewModel
import com.simprints.moduleapi.face.requests.IFaceRequest
import timber.log.Timber

class FaceOrchestratorViewModel : ViewModel() {
    fun start(faceRequest: IFaceRequest) {
        Timber.d("FaceRequest = $faceRequest")
    }
}
