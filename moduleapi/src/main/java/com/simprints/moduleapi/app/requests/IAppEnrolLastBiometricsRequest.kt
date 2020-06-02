package com.simprints.moduleapi.app.requests

interface IAppEnrolLastBiometricsRequest : IAppRequest {
    val moduleId: String
    val metadata: String
    val sessionId: String
}

