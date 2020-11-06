package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.*
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFollowUp.AppConfirmIdentityRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFollowUp.AppEnrolLastBiometricsRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.EnrolmentHelper
import com.simprints.id.orchestrator.responsebuilders.adjudication.EnrolAdjudicationAction
import com.simprints.id.orchestrator.responsebuilders.adjudication.EnrolResponseAdjudicationHelper
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.tools.time.TimeHelper

class AppResponseFactoryImpl(
    private val enrolmentHelper: EnrolmentHelper,
    private val timeHelper: TimeHelper,
    private val isEnrolmentPlus: Boolean,
    private val fingerprintConfidenceThresholds: Map<FingerprintConfidenceThresholds, Int>,
    private val faceConfidenceThresholds: Map<FaceConfidenceThresholds, Int>,
    private val returnIdentificationCount: Int,
    private val enrolResponseAdjudicationHelper: EnrolResponseAdjudicationHelper
) : AppResponseFactory {

    override suspend fun buildAppResponse(modalities: List<Modality>,
                                          appRequest: AppRequest,
                                          steps: List<Step>,
                                          sessionId: String): AppResponse =
        when (appRequest) {
            is AppEnrolRequest -> performAdjudicationForEnrolRequest(steps).run {
                when(this) {
                    EnrolAdjudicationAction.ENROL -> AppResponseBuilderForEnrol(enrolmentHelper, timeHelper)
                    EnrolAdjudicationAction.IDENTIFY -> buildAppResponseBuilderForIdentify()
                }
            }
            is AppIdentifyRequest -> buildAppResponseBuilderForIdentify()
            is AppVerifyRequest -> AppResponseBuilderForVerify(fingerprintConfidenceThresholds, faceConfidenceThresholds)
            is AppConfirmIdentityRequest -> AppResponseBuilderForConfirmIdentity()
            is AppEnrolLastBiometricsRequest -> AppResponseBuilderForEnrolLastBiometrics()
        }.buildAppResponse(modalities, appRequest, steps, sessionId)

    private fun performAdjudicationForEnrolRequest(steps: List<Step>) =
        enrolResponseAdjudicationHelper.getAdjudicationAction(isEnrolmentPlus, steps)

    private fun buildAppResponseBuilderForIdentify() =
        AppResponseBuilderForIdentify(fingerprintConfidenceThresholds, faceConfidenceThresholds, returnIdentificationCount)
}

