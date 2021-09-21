package com.simprints.face.data.moduleapi.face.requests

import kotlinx.parcelize.Parcelize

@Parcelize
data class FaceConfigurationRequest(val projectId: String, val deviceId: String) : FaceRequest
