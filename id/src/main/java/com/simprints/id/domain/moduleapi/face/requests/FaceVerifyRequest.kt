package com.simprints.id.domain.moduleapi.face.requests

import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceVerifyRequest(val projectId: String,
                             val userId: String,
                             val moduleId: String) : FaceRequest

