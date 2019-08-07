package com.simprints.id.orchestrator.modality

import android.content.Intent
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.orchestrator.steps.Step


interface ModalityFlow {

    val steps: List<Step>

    fun startFlow(appRequest: AppRequest, modalities: List<Modality>)

    fun getNextStepToStart(): Step?

    fun handleIntentResult(requestCode: Int, resultCode: Int, data: Intent?)
}
