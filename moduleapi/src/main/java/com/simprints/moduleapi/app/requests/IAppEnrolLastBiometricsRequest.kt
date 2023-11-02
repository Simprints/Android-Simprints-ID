package com.simprints.moduleapi.app.requests

interface IAppEnrolLastBiometricsRequest : IAppRequest {
    val moduleId: String
    val isModuleIdTokenized: Boolean
    val metadata: String
    val sessionId: String
}

