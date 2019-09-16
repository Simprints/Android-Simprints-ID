package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppEnrolResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
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
        val faceResponse = getFaceCaptureResponse(results)
        val fingerprintResponse = getFingerprintCaptureResponse(results)

        return when {
            fingerprintResponse != null && faceResponse != null -> {
                buildAppEnrolResponseForFingerprintAndFace(fingerprintResponse, faceResponse)
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

    private fun getFaceCaptureResponse(results: List<Step.Result?>): FaceCaptureResponse? =
        results.firstOrNull { it is FaceCaptureResponse } as FaceCaptureResponse?

    private fun getFingerprintCaptureResponse(results: List<Step.Result?>): FingerprintEnrolResponse? =
        results.firstOrNull { it is FingerprintEnrolResponse } as FingerprintEnrolResponse

    private fun buildAppEnrolResponseForFingerprintAndFace(fingerprintResponse: FingerprintEnrolResponse,
                                                           faceResponse: FaceCaptureResponse) =
        AppEnrolResponse(fingerprintResponse.guid)

    private fun buildAppEnrolResponseForFingerprint(fingerprintResponse: FingerprintEnrolResponse) =
        AppEnrolResponse(fingerprintResponse.guid)

    private fun buildAppEnrolResponseForFace(faceResponse: FaceCaptureResponse): AppEnrolResponse {
        TODO("Not implemented yet")
    }
}
