package com.simprints.id.orchestrator

import android.content.Intent
import androidx.lifecycle.LiveData
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.steps.Step

/**
 * It produces [Step]s to run and finally creates an [AppResponse] to return
 * to the the ClientApi
 */
interface OrchestratorManager {

    val onGoingStep: LiveData<Step?>
    val appResponse: LiveData<AppResponse?>

    fun initialise(modalities: List<Modality>, appRequest: AppRequest, sessionId: String)
    fun handleIntentResult(requestCode: Int, resultCode: Int, data: Intent?)

    fun restoreState(steps: List<Step>)
    fun getState(): List<Step>
}
