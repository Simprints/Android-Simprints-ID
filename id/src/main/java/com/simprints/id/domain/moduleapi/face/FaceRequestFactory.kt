package com.simprints.id.domain.moduleapi.face

import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceEnrolRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceIdentifyRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceVerifyRequest

object FaceRequestFactory {

    fun buildFaceRequest(appRequest: AppRequest): FaceRequest =
        when (appRequest) {
            is AppEnrolRequest -> buildFaceEnrolRequest(appRequest)
            is AppVerifyRequest -> buildFaceVerifyRequest(appRequest)
            is AppIdentifyRequest -> buildFaceIdentifyRequest(appRequest)
            else -> throw IllegalStateException("Invalid fingerprint request")
        }

    private fun buildFaceEnrolRequest(enrol: AppEnrolRequest): FaceEnrolRequest =
        with(enrol) {
            FaceEnrolRequest(projectId, userId, moduleId)
        }

    private fun buildFaceVerifyRequest(verify: AppVerifyRequest): FaceVerifyRequest =
        with(verify) {
            FaceVerifyRequest(projectId, userId, moduleId)
        }

    private fun buildFaceIdentifyRequest(identify: AppIdentifyRequest): FaceIdentifyRequest =
        with(identify) {
            FaceIdentifyRequest(projectId, userId, moduleId)
        }
}
