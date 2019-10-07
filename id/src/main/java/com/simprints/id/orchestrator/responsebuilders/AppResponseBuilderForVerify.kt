package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.app.responses.AppVerifyResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceVerifyResponse
import com.simprints.id.orchestrator.steps.Step

class AppResponseBuilderForVerify : BaseAppResponseBuilder() {

    override suspend fun buildAppResponse(modalities: List<Modality>,
                                          appRequest: AppRequest,
                                          steps: List<Step>,
                                          sessionId: String): AppResponse {
        super.getErrorOrRefusalResponseIfAny(steps)?.let {
            return it
        }

        val results = steps.map { it.result }
        val faceResponse = getFaceResponseForVerify(results)
        val fingerprintResponse = getFingerprintResponseForVerify(results)

        return when {
            fingerprintResponse != null && faceResponse != null -> {
                buildAppVerifyResponseForFingerprintAndFace(faceResponse, fingerprintResponse)
            }
            fingerprintResponse != null -> {
                buildAppVerifyResponseForFingerprint(fingerprintResponse)
            }
            faceResponse != null -> {
                buildAppVerifyResponseForFace(faceResponse)
            }
            else -> throw Throwable("All responses are null")
        }
    }

    private fun getFaceResponseForVerify(results: List<Step.Result?>): FaceVerifyResponse? =
        results.filterIsInstance(FaceVerifyResponse::class.java).lastOrNull()

    private fun getFingerprintResponseForVerify(results: List<Step.Result?>): FingerprintVerifyResponse? =
        results.filterIsInstance(FingerprintVerifyResponse::class.java).lastOrNull()

    private fun buildAppVerifyResponseForFingerprintAndFace(faceResponse: FaceVerifyResponse,
                                                            fingerprintResponse: FingerprintVerifyResponse) =
        AppVerifyResponse(fingerprintResponse.matchingResult.toAppMatchResult())

    private fun buildAppVerifyResponseForFingerprint(fingerprintResponse: FingerprintVerifyResponse) =
        AppVerifyResponse(fingerprintResponse.matchingResult.toAppMatchResult())

    private fun buildAppVerifyResponseForFace(faceResponse: FaceVerifyResponse): AppVerifyResponse {
        TODO("Not implemented yet")
    }
}
