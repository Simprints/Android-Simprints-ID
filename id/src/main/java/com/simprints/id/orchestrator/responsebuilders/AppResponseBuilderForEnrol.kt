package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppEnrolResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceEnrolResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintEnrolResponse
import com.simprints.id.orchestrator.steps.Step

class AppResponseBuilderForEnrol : AppResponseBuilder, BaseAppResponseBuilder() {

    override fun buildAppResponse(modalities: List<Modality>,
                                  appRequest: AppRequest,
                                  steps: List<Step>,
                                  sessionId: String): AppResponse {

        super.getErrorOrRefusalResponseIfAny(steps)?.let {
            return it
        }

        val results = steps.map { it.result }
        val faceResponse = getFaceResponseForEnrol(results)
        val fingerprintResponse = getFingerprintResponseForEnrol(results)

        return when {
            fingerprintResponse != null && faceResponse != null -> {
                buildAppEnrolResponseForFingerprintAndFace(faceResponse, fingerprintResponse)
            }
            fingerprintResponse != null -> {
                buildAppEnrolResponseForFingerprint(fingerprintResponse)
            }
            faceResponse != null -> {
                buildAppEnrolResponseForFace(faceResponse)
            }
            else -> throw Throwable("App responses are null")
        }
    }

    private fun getFaceResponseForEnrol(results: List<Step.Result?>): FaceEnrolResponse? =
        results.firstOrNull { it is FaceEnrolResponse } as FaceEnrolResponse

    private fun getFingerprintResponseForEnrol(results: List<Step.Result?>): FingerprintEnrolResponse? =
        results.firstOrNull { it is FingerprintEnrolResponse } as FingerprintEnrolResponse

    private fun buildAppEnrolResponseForFingerprintAndFace(fingerprintResponse: FaceEnrolResponse,
                                                           faceResponse: FingerprintEnrolResponse) =
        AppEnrolResponse(fingerprintResponse.guid)

    private fun buildAppEnrolResponseForFingerprint(fingerprintResponse: FingerprintEnrolResponse) =
        AppEnrolResponse(fingerprintResponse.guid)

    private fun buildAppEnrolResponseForFace(faceResponse: FaceEnrolResponse) =
        AppEnrolResponse(faceResponse.guid)
}
