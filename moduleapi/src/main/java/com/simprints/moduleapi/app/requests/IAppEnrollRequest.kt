package com.simprints.moduleapi.app.requests


interface IAppEnrollRequest : IAppRequest {
    val moduleId: String
    val metadata: String
}
