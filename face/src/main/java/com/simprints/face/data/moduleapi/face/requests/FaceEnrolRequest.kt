package com.simprints.face.data.moduleapi.face.requests

import com.simprints.id.domain.moduleapi.face.requests.FaceRequest
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceEnrolRequest(val projectId: String,
                            val userId: String,
                            val moduleId: String) : FaceRequest
