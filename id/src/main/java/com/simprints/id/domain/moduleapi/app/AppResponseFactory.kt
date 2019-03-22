package com.simprints.id.domain.moduleapi.app

import com.simprints.face.data.moduleapi.face.responses.FaceIdentifyResponse
import com.simprints.face.data.moduleapi.face.responses.entities.toAppMatchResult
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.id.domain.moduleapi.app.responses.AppEnrolResponse
import com.simprints.id.domain.moduleapi.app.responses.AppIdentifyResponse
import com.simprints.id.domain.moduleapi.app.responses.AppVerifyResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceEnrolResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceVerifyResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintEnrolResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintIdentifyResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintVerifyResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.toAppMatchResult

class AppResponseFactory {

    class BuilderForFingerprint {

        fun buildAppResponse(appRequest: AppRequest,
                             fingerprintResponse: FingerprintResponse,
                             sessionId: String = "") =
            when (appRequest) {
                is AppEnrolRequest -> buildAppEnrolResponse(fingerprintResponse as FingerprintEnrolResponse)
                is AppIdentifyRequest -> {
                    require(sessionId.isNotEmpty())
                    buildAppIdentifyResponse(fingerprintResponse as FingerprintIdentifyResponse, sessionId)
                }
                is AppVerifyRequest -> buildAppVerifyResponse(fingerprintResponse as FingerprintVerifyResponse)
                else -> throw Throwable("Invalid AppRequest")
            }

        private fun buildAppIdentifyResponse(fingerprintResponse: FingerprintIdentifyResponse, sessionId: String): AppIdentifyResponse =
            AppIdentifyResponse(fingerprintResponse.identifications.map { it.toAppMatchResult() }, sessionId)

        private fun buildAppVerifyResponse(fingerprintResponse: FingerprintVerifyResponse): AppVerifyResponse =
            AppVerifyResponse(fingerprintResponse.matchingResult.toAppMatchResult())

        private fun buildAppEnrolResponse(fingerprintResponse: FingerprintEnrolResponse): AppEnrolResponse =
            AppEnrolResponse(fingerprintResponse.guid)
    }

    class BuilderForFace {

        fun buildAppResponse(appRequest: AppRequest,
                             faceResponse: FaceResponse,
                             sessionId: String = "") =
            when (appRequest) {
                is AppEnrolRequest -> buildAppEnrolResponse(faceResponse as FaceEnrolResponse)
                is AppIdentifyRequest -> {
                    require(sessionId.isNotEmpty())
                    buildAppIdentifyResponse(faceResponse as FaceIdentifyResponse, sessionId)
                }
                is AppVerifyRequest -> buildAppVerifyResponse(faceResponse as FaceVerifyResponse)
                else -> throw Throwable("Invalid AppRequest")
            }

        private fun buildAppIdentifyResponse(FaceResponse: FaceIdentifyResponse, sessionId: String): AppIdentifyResponse =
            AppIdentifyResponse(FaceResponse.identifications.map { it.toAppMatchResult() }, sessionId)

        private fun buildAppVerifyResponse(FaceResponse: FaceVerifyResponse): AppVerifyResponse =
            AppVerifyResponse(FaceResponse.matchingResult.toAppMatchResult())

        private fun buildAppEnrolResponse(FaceResponse: FaceEnrolResponse): AppEnrolResponse =
            AppEnrolResponse(FaceResponse.guid)
    }

    //TODO: For now I am ignoring the face response
    class BuilderForFingerFace {

        fun buildAppResponse(appRequest: AppRequest,
                             fingerprintResponse:
                             FingerprintResponse, faceResponse: FaceResponse,
                             sessionId: String = "") =
            when (appRequest) {
                is AppEnrolRequest -> buildAppEnrolResponse(fingerprintResponse as FingerprintEnrolResponse, faceResponse as FaceEnrolResponse)
                is AppIdentifyRequest -> {
                    require(sessionId.isNotEmpty())
                    buildAppIdentifyResponse(fingerprintResponse as FingerprintIdentifyResponse, faceResponse as FaceIdentifyResponse, sessionId!!)
                }
                is AppVerifyRequest -> buildAppVerifyResponse(fingerprintResponse as FingerprintVerifyResponse, faceResponse as FaceVerifyResponse)
                else -> throw Throwable("Invalid AppRequest")
            }

        private fun buildAppIdentifyResponse(fingerprintResponse: FingerprintIdentifyResponse,
                                             faceResponse: FaceIdentifyResponse, sessionId: String): AppIdentifyResponse =
            AppIdentifyResponse(fingerprintResponse.identifications.map { it.toAppMatchResult() }, sessionId)

        private fun buildAppVerifyResponse(fingerprintResponse: FingerprintVerifyResponse,
                                           faceResponse: FaceVerifyResponse): AppVerifyResponse =
            AppVerifyResponse(fingerprintResponse.matchingResult.toAppMatchResult())

        private fun buildAppEnrolResponse(fingerprintResponse: FingerprintEnrolResponse,
                                          faceResponse: FaceEnrolResponse): AppEnrolResponse =
            AppEnrolResponse(fingerprintResponse.guid)
    }

    //TODO: For now I am ignoring the face response
    class BuilderForFaceFinger {

        fun buildAppResponse(appRequest: AppRequest,
                             fingerprintResponse:
                             FingerprintResponse, faceResponse: FaceResponse,
                             sessionId: String = "") =
            when (appRequest) {
                is AppEnrolRequest -> buildAppEnrolResponse(faceResponse as FaceEnrolResponse, fingerprintResponse as FingerprintEnrolResponse)
                is AppIdentifyRequest -> {
                    require(sessionId.isNotEmpty())
                    buildAppIdentifyResponse(faceResponse as FaceIdentifyResponse, fingerprintResponse as FingerprintIdentifyResponse, sessionId)
                }
                is AppVerifyRequest -> buildAppVerifyResponse(faceResponse as FaceVerifyResponse, fingerprintResponse as FingerprintVerifyResponse)
                else -> throw Throwable("Invalid AppRequest")
            }

        private fun buildAppIdentifyResponse(faceResponse: FaceIdentifyResponse,
                                             fingerprintResponse: FingerprintIdentifyResponse, sessionId: String): AppIdentifyResponse =
            AppIdentifyResponse(fingerprintResponse.identifications.map { it.toAppMatchResult() }, sessionId)

        private fun buildAppVerifyResponse(faceResponse: FaceVerifyResponse,
                                           fingerprintResponse: FingerprintVerifyResponse): AppVerifyResponse =
            AppVerifyResponse(fingerprintResponse.matchingResult.toAppMatchResult())

        private fun buildAppEnrolResponse(faceResponse: FaceEnrolResponse,
                                          fingerprintResponse: FingerprintEnrolResponse): AppEnrolResponse =
            AppEnrolResponse(fingerprintResponse.guid)
    }
}
