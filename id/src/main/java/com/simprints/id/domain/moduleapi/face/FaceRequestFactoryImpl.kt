package com.simprints.id.domain.moduleapi.face

import com.simprints.id.domain.moduleapi.face.requests.FaceEnrolRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceIdentifyRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceVerifyRequest

class FaceRequestFactoryImpl: FaceRequestFactory {

    override fun buildFaceEnrolRequest(projectId: String,
                                      userId: String,
                                      moduleId: String) = FaceEnrolRequest(projectId, userId, moduleId)

    override fun buildFaceVerifyRequest(projectId: String,
                                       userId: String,
                                       moduleId: String) = FaceVerifyRequest(projectId, userId, moduleId)

    override fun buildFaceIdentifyRequest(projectId: String,
                                         userId: String,
                                         moduleId: String) = FaceIdentifyRequest(projectId, userId, moduleId)
}
