package com.simprints.id.orchestrator.modality.builders

import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.domain.modality.Modality.FINGER
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.Step

class AppResponseFactoryImpl : AppResponseFactory {

    override fun buildAppResponse(modalities: List<Modality>,
                                  appRequest: AppRequest,
                                  steps: List<Step>,
                                  sessionId: String): AppResponse =
        when (modalities) {
            listOf(FINGER) -> AppResponseBuilderForFinger()
            listOf(FINGER, FACE) -> AppResponseBuilderForFingerFace()
            listOf(FACE, FINGER) -> AppResponseBuilderForFaceFinger()
            listOf(FACE) -> AppResponseBuilderForFace()
            else -> null
        }?.buildResponse(appRequest, steps, sessionId) ?: throw Throwable("Wrong modalities")
}

