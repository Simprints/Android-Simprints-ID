package com.simprints.id.orchestrator

import android.content.Intent
import androidx.lifecycle.LiveData
import com.simprints.core.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.steps.Step

/**
 * It produces [Step]s to run and finally creates an [AppResponse] to return
 * to the the ClientApi
 */
interface OrchestratorManager {

    val ongoingStep: LiveData<Step?>
    val appResponse: LiveData<AppResponse?>

    suspend fun initialise(modalities: List<Modality>, appRequest: AppRequest, sessionId: String)
    suspend fun handleIntentResult(appRequest: AppRequest, requestCode: Int, resultCode: Int, data: Intent?)

    suspend fun restoreState()
    fun saveState()
}
