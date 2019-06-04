package com.simprints.id.orchestrator.modality.builders

import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.modality.Modality.*
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow

class AppResponseFactoryImpl : AppResponseFactory {

    override fun buildAppResponse(modality: Modality,
                                  appRequest: AppRequest,
                                  modalityResponses: List<ModalityFlow.Step>,
                                  sessionId: String): AppResponse =
        when (modality) {
            FACE -> AppResponseBuilderForFace()
            FINGER -> AppResponseBuilderForFinger()
            FINGER_FACE -> AppResponseBuilderForFingerFace()
            FACE_FINGER -> AppResponseBuilderForFaceFinger()
        }.buildResponse(appRequest, modalityResponses.mapNotNull { it.response }, sessionId)
}

