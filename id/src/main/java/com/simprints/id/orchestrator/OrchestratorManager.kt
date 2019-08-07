package com.simprints.id.orchestrator

import android.content.Intent
import androidx.lifecycle.LiveData
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.steps.Step

interface OrchestratorManager {

    val nextIntent: LiveData<Step.Request>
    val appResponse: LiveData<AppResponse>

    suspend fun start(modalities: List<Modality>, appRequest: AppRequest, sessionId:String)
    suspend fun onModalStepRequestDone(requestCode: Int, resultCode: Int, data: Intent?)
}
