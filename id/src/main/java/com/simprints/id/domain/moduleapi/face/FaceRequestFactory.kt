package com.simprints.id.domain.moduleapi.face

import com.simprints.id.domain.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceIdentifyRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceVerifyRequest

interface FaceRequestFactory {

    fun buildCaptureRequest(nFaceSamplesToCapture: Int): FaceCaptureRequest

    fun buildFaceVerifyRequest(projectId: String,
                               userId: String,
                               moduleId: String): FaceVerifyRequest

    fun buildFaceIdentifyRequest(projectId: String,
                                 userId: String,
                                 moduleId: String): FaceIdentifyRequest
}
