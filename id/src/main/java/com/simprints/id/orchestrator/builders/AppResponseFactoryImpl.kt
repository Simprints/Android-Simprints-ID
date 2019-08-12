package com.simprints.id.orchestrator.builders

import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.domain.modality.Modality.FINGER
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.steps.Step

class AppResponseFactoryImpl : AppResponseFactory {

    override fun buildAppResponse(modalities: List<Modality>,
                                  appRequest: AppRequest,
                                  steps: List<Step>,
                                  sessionId: String): AppResponse =
        when (modalities) {
            /**
             * Currently only FINGER/AppResponseBuilderForFinger is used. The others
             * are placeholders for when we will introduce the FaceModality
             */
            listOf(FINGER) -> AppResponseBuilderForFinger()
            listOf(FINGER, FACE) -> AppResponseBuilderForFingerFace()
            listOf(FACE, FINGER) -> AppResponseBuilderForFaceFinger()
            listOf(FACE) -> AppResponseBuilderForFace()
            else -> null
        }?.buildResponse(appRequest, steps, sessionId) ?: throw Throwable("Wrong modalities")
}

