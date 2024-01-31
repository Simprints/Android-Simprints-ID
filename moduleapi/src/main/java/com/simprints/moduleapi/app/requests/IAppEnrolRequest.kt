package com.simprints.moduleapi.app.requests


interface IAppEnrolRequest : IAppRequest {
    val moduleId: String
    val isModuleIdTokenized: Boolean
    val metadata: String
}