package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.steps.Step

class AppResponseFactoryImpl : AppResponseFactory {

    override fun buildAppResponse(modalities: List<Modality>,
                                  appRequest: AppRequest,
                                  steps: List<Step>,
                                  sessionId: String): AppResponse =
        /**
         * Currently only FINGER/AppResponseBuilderForFinger is used. The others
         * are placeholders for when we will introduce the FaceModality
         */
        when (appRequest) {
            is AppEnrolRequest -> AppResponseBuilderForEnrol()
            is AppIdentifyRequest -> AppResponseBuilderForIdentify()
            is AppVerifyRequest -> AppResponseBuilderForVerify()
            else -> null
        }?.buildAppResponse(modalities, appRequest, steps, sessionId)
            ?: throw Throwable("Wrong modalities")
}

