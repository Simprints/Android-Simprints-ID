package com.simprints.id.domain.moduleapi.face.requests

import com.simprints.id.domain.moduleapi.face.requests.FaceRequestType.*
import com.simprints.id.orchestrator.steps.Step.Request

interface FaceRequest: Request {
    val type: FaceRequestType
}

enum class FaceRequestType {
    CAPTURE,
    MATCH,

    VERIFY, //TBRemoved soon
    IDENTIFY //TBRemoved soon
}

fun FaceRequest.fromDomainToModuleApi() =
    when (this.type) {
        CAPTURE -> (this as FaceCaptureRequest).fromDomainToModuleApi()
        MATCH -> TODO()
        VERIFY -> (this as FaceVerifyRequest).fromDomainToModuleApi() //TBRemoved soon
        IDENTIFY -> (this as FaceIdentifyRequest).fromDomainToModuleApi() //TBRemoved soon
    }
