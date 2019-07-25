package com.simprints.id.orchestrator.modality.builders

import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.modality.Modality.*
import com.simprints.id.domain.modality.ModalityResponse
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse

class AppResponseFactoryImpl : AppResponseFactory {

    override fun buildAppResponse(modality: Modality,
                                  appRequest: AppRequest,
                                  modalityResponses: List<ModalityResponse>,
                                  sessionId: String): AppResponse =
        when (modality) {
            FACE -> AppResponseBuilderForFace()
            FINGER -> AppResponseBuilderForFinger()
            FINGER_FACE -> AppResponseBuilderForFingerFace()
            FACE_FINGER -> AppResponseBuilderForFaceFinger()
        }.buildResponse(appRequest, modalityResponses, sessionId)
}

