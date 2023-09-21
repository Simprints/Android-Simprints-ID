package com.simprints.id.domain.moduleapi.face.requests

import com.simprints.id.orchestrator.steps.Step.Request

interface FaceRequest : Request {
    val type: FaceRequestType
}

enum class FaceRequestType {
    CAPTURE,
    MATCH,
}


