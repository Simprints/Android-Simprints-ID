package com.simprints.moduleapi.face.requests

interface IFaceConfigurationRequest : IFaceRequest {
    val projectId: String
    val deviceId: String
}

