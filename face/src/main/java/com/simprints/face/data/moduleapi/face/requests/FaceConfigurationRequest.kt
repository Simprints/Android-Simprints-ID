package com.simprints.face.data.moduleapi.face.requests

import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceConfigurationRequest(val projectId: String, val deviceId: String) : FaceRequest
