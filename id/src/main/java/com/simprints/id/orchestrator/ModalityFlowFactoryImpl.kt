package com.simprints.id.orchestrator

import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequestType
import com.simprints.id.orchestrator.modality.ModalityFlow

class ModalityFlowFactoryImpl(private val enrolFlow: ModalityFlow,
                              private val verifyFlow: ModalityFlow,
                              private val identifyFlow: ModalityFlow) : ModalityFlowFactory {


    override fun createModalityFlow(appRequest: AppRequest,
                                    modalities: List<Modality>): ModalityFlow =
        buildFlow(appRequest).also {
            it.startFlow(appRequest, modalities)
        }

    private fun buildFlow(appRequest: AppRequest): ModalityFlow =
        when (appRequest.type) {
            AppRequestType.ENROL -> enrolFlow
            AppRequestType.IDENTIFY ->identifyFlow
            AppRequestType.VERIFY -> verifyFlow
        }
}
