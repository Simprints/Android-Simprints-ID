package com.simprints.id.domain.moduleapi.face.requests

import com.simprints.id.orchestrator.steps.Step.Request

interface FaceRequest : Request {
    val type: FaceRequestType
}

enum class FaceRequestType {
    CAPTURE,
    MATCH,
    CONFIGURATION
}

fun FaceRequest.fromDomainToModuleApi() = when (type) {
    FaceRequestType.CAPTURE -> (this as FaceCaptureRequest).fromDomainToModuleApi()
    FaceRequestType.MATCH -> (this as FaceMatchRequest).fromDomainToModuleApi()
    FaceRequestType.CONFIGURATION -> (this as FaceConfigurationRequest).fromDomainToModuleApi()
}


