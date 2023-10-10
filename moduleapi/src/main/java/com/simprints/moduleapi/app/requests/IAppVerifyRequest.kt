package com.simprints.moduleapi.app.requests


interface IAppVerifyRequest : IAppRequest {

    val moduleId: String
    val isModuleIdTokenized: Boolean
    val metadata: String
    val verifyGuid: String

}
