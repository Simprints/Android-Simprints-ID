package com.simprints.id.orchestrator.modality

import android.content.Intent
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.orchestrator.steps.Step

/**
 * It manages the list of steps to execute (launch Activities) to complete a flow (Enrol, Verify, Identify).
 * The steps are based on the action (AppRequest) and the list of Modalities to execute.
 * Every time an Activity ends, then the result is handled in [ModalityFlow.handleIntentResult] and the steps
 * are updated.
 */
interface ModalityFlow {

    val steps: List<Step>

    fun startFlow(appRequest: AppRequest, modalities: List<Modality>)

    fun getNextStepToLaunch(): Step?

    fun handleIntentResult(requestCode: Int, resultCode: Int, data: Intent?)

    fun restoreState(stepsToRestore: List<Step>)
}
