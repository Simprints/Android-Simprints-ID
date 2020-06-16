package com.simprints.id.orchestrator

import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.*
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFollowUp.AppConfirmIdentityRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFollowUp.AppEnrolLastBiometricsRequest
import com.simprints.id.orchestrator.modality.ModalityFlow

class ModalityFlowFactoryImpl(private val enrolFlow: ModalityFlow,
                              private val verifyFlow: ModalityFlow,
                              private val identifyFlow: ModalityFlow,
                              private val confirmationIdentityFlow: ModalityFlow,
                              private val enrolLastBiometricsFlow: ModalityFlow) : ModalityFlowFactory {


    override fun createModalityFlow(appRequest: AppRequest,
                                    modalities: List<Modality>): ModalityFlow =
        buildFlow(appRequest).also {
            it.startFlow(appRequest)
        }

    private fun buildFlow(appRequest: AppRequest): ModalityFlow =
        when (appRequest) {
            is AppEnrolRequest -> enrolFlow
            is AppIdentifyRequest -> identifyFlow
            is AppVerifyRequest -> verifyFlow
            is AppConfirmIdentityRequest -> confirmationIdentityFlow
            is AppEnrolLastBiometricsRequest -> enrolLastBiometricsFlow
        }
}
