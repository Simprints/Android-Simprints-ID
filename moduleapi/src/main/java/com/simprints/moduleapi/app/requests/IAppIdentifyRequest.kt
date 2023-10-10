package com.simprints.moduleapi.app.requests


interface IAppIdentifyRequest : IAppRequest {
    val moduleId: String
    val isModuleIdTokenized: Boolean
    val metadata: String
}
