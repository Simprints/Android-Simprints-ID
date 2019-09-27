package com.simprints.id.domain.moduleapi.face

import com.simprints.id.domain.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceIdentifyRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceVerifyRequest

class FaceRequestFactoryImpl: FaceRequestFactory {

    override fun buildCaptureRequest(nFaceSamplesToCapture: Int) = FaceCaptureRequest(nFaceSamplesToCapture)

    override fun buildFaceVerifyRequest(projectId: String,
                                       userId: String,
                                       moduleId: String) = FaceVerifyRequest(projectId, userId, moduleId)

    override fun buildFaceIdentifyRequest(projectId: String,
                                         userId: String,
                                         moduleId: String) = FaceIdentifyRequest(projectId, userId, moduleId)
}
