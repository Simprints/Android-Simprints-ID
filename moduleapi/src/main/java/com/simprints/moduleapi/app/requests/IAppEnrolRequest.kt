package com.simprints.moduleapi.app.requests


interface IAppEnrolRequest : IAppRequest {
    val moduleId: String
    val metadata: String
}
