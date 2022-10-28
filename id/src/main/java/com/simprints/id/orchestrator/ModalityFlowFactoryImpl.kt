package com.simprints.id.orchestrator

import com.simprints.id.di.*
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.*
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFollowUp.AppConfirmIdentityRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFollowUp.AppEnrolLastBiometricsRequest
import com.simprints.id.orchestrator.modality.ModalityFlow
import javax.inject.Inject

class ModalityFlowFactoryImpl @Inject constructor(
    @ModalityFlowEnrolment private val enrolFlow: ModalityFlow,
    @ModalityFlowVerification private val verifyFlow: ModalityFlow,
    @ModalityFlowIdentification private val identifyFlow: ModalityFlow,
    @ModalityFlowConfirmation private val confirmationIdentityFlow: ModalityFlow,
    @ModalityFlowEnrolmentLastBiometrics private val enrolLastBiometricsFlow: ModalityFlow
) : ModalityFlowFactory {


    override suspend fun createModalityFlow(appRequest: AppRequest): ModalityFlow =
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
