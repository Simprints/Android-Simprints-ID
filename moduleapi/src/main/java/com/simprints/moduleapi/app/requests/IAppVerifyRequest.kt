package com.simprints.moduleapi.app.requests


interface IAppVerifyRequest : IAppRequest {

    val moduleId: String
    val metadata: String
    val verifyGuid: String

}
