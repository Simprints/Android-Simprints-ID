package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.app.responses.AppVerifyResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceVerifyResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.toAppMatchResult
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintVerifyResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.toAppMatchResult
import com.simprints.id.orchestrator.steps.Step

class AppResponseBuilderForVerify : AppResponseBuilder {

    override fun buildAppResponse(modalities: List<Modality>,
                                  appRequest: AppRequest,
                                  steps: List<Step>,
                                  sessionId: String): AppResponse {

        val results = steps.map { it.result }
        val faceResponse = getFaceResponseForVerify(modalities, results)
        val fingerprintResponse = getFingerprintResponseForVerify(modalities, results)

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

    private fun getFaceResponseForVerify(modalities: List<Modality>, results: List<Step.Result?>): FaceVerifyResponse? {
        val index =  modalities.indexOf(Modality.FACE)
        return if (index > -1) {
            results[index] as FaceVerifyResponse
        } else {
            null
        }
    }

    private fun getFingerprintResponseForVerify(modalities: List<Modality>, results: List<Step.Result?>): FingerprintVerifyResponse? {
        val index =  modalities.indexOf(Modality.FACE)
        return if (index > -1) {
            results[index] as FingerprintVerifyResponse
        } else {
            null
        }
    }

    private fun buildAppVerifyResponseForFingerprintAndFace(faceResponse: FaceVerifyResponse,
                                                            fingerprintResponse: FingerprintVerifyResponse) =
        AppVerifyResponse(fingerprintResponse.matchingResult.toAppMatchResult())

    private fun buildAppVerifyResponseForFingerprint(fingerprintResponse: FingerprintVerifyResponse) =
        AppVerifyResponse(fingerprintResponse.matchingResult.toAppMatchResult())

    private fun buildAppVerifyResponseForFace(faceResponse: FaceVerifyResponse) =
        AppVerifyResponse(faceResponse.matchingResult.toAppMatchResult())
}
