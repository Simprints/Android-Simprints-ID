package com.simprints.id.domain.moduleapi.face

import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceRequest

interface FaceRequestFactory {

    fun buildFaceRequest(appRequest: AppRequest): FaceRequest
}
