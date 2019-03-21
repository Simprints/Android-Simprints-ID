package com.simprints.id.domain.moduleapi.app

import com.simprints.face.data.moduleapi.face.responses.FaceIdentifyResponse
import com.simprints.face.data.moduleapi.face.responses.entities.toAppMatchResult
import com.simprints.id.domain.moduleapi.app.responses.AppEnrolResponse
import com.simprints.id.domain.moduleapi.app.responses.AppIdentifyResponse
import com.simprints.id.domain.moduleapi.app.responses.AppVerifyResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceEnrolResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceVerifyResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintEnrolResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintIdentifyResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintVerifyResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.toAppMatchResult

object AppResponseFactory {


     fun buildAppIdentifyResponse(fingerprintResponse: FingerprintIdentifyResponse, sessionId: String): AppIdentifyResponse =
        AppIdentifyResponse(fingerprintResponse.identifications.map { it.toAppMatchResult() }, sessionId)

     fun buildAppVerifyResponse(fingerprintResponse: FingerprintVerifyResponse): AppVerifyResponse =
        AppVerifyResponse(fingerprintResponse.matchingResult.toAppMatchResult())

     fun buildAppEnrolResponse(fingerprintResponse: FingerprintEnrolResponse): AppEnrolResponse =
        AppEnrolResponse(fingerprintResponse.guid)

     fun buildAppIdentifyResponse(FaceResponse: FaceIdentifyResponse, sessionId: String): AppIdentifyResponse =
        AppIdentifyResponse(FaceResponse.identifications.map { it.toAppMatchResult() }, sessionId)

     fun buildAppVerifyResponse(FaceResponse: FaceVerifyResponse): AppVerifyResponse =
        AppVerifyResponse(FaceResponse.matchingResult.toAppMatchResult())

     fun buildAppEnrolResponse(FaceResponse: FaceEnrolResponse): AppEnrolResponse =
        AppEnrolResponse(FaceResponse.guid)

    fun buildAppIdentifyResponse(fingerprintResponse: FingerprintIdentifyResponse, faceResponse: FaceIdentifyResponse, sessionId: String): AppIdentifyResponse =
        AppIdentifyResponse(fingerprintResponse.identifications.map { it.toAppMatchResult() }, sessionId)

    fun buildAppVerifyResponse(fingerprintResponse: FingerprintVerifyResponse, faceResponse: FaceVerifyResponse): AppVerifyResponse =
        AppVerifyResponse(fingerprintResponse.matchingResult.toAppMatchResult())

    fun buildAppEnrolResponse(fingerprintResponse: FingerprintEnrolResponse, faceResponse: FaceEnrolResponse): AppEnrolResponse =
        AppEnrolResponse(fingerprintResponse.guid)
}
