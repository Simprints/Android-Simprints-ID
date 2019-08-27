package com.simprints.id.domain.moduleapi.face

import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceEnrolRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceIdentifyRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceVerifyRequest

interface FaceRequestFactory {

    fun buildFaceEnrolRequest(projectId: String,
                              userId: String,
                              moduleId: String): FaceEnrolRequest

    fun buildFaceVerifyRequest(projectId: String,
                               userId: String,
                               moduleId: String): FaceVerifyRequest

    fun buildFaceIdentifyRequest(projectId: String,
                                 userId: String,
                                 moduleId: String): FaceIdentifyRequest
}

fun FaceRequestFactory.buildFaceRequest(appRequest: AppRequest): FaceRequest =
    with(appRequest) {
        when (this) {
            is AppEnrolRequest -> buildFaceEnrolRequest(projectId, userId, moduleId)
            is AppVerifyRequest -> buildFaceVerifyRequest(projectId, userId, moduleId)
            is AppIdentifyRequest -> buildFaceIdentifyRequest(projectId, userId, moduleId)
            else -> throw IllegalStateException("Invalid fingerprint request")
        }
    }
