package com.simprints.moduleapi.app.requests


interface IAppIdentifyRequest : IAppRequest {
    val moduleId: String
    val metadata: String
}
