package com.simprints.id.orchestrator

import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequestAction.*
import com.simprints.id.orchestrator.modality.ModalityFlow

class ModalityFlowFactoryImpl(private val enrolFlow: ModalityFlow,
                              private val verifyFlow: ModalityFlow,
                              private val identifyFlow: ModalityFlow) : ModalityFlowFactory {


    override fun startModalityFlow(appRequest: AppRequest,
                                   modalities: List<Modality>): ModalityFlow =
        buildFlow(appRequest).also {
            it.startFlow(appRequest, modalities)
        }

    private fun buildFlow(appRequest: AppRequest): ModalityFlow =
        when (AppRequest.action(appRequest)) {
            ENROL -> enrolFlow
            IDENTIFY -> identifyFlow
            VERIFY -> verifyFlow
            CONFIRM -> throw Throwable("Confirm identity is not handled by Orchestrator")
        }
}
