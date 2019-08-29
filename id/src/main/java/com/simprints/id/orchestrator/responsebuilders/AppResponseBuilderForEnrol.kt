package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppEnrolResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceEnrolResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintEnrolResponse
import com.simprints.id.orchestrator.steps.Step

class AppResponseBuilderForEnrol : AppResponseBuilder {

    override fun buildAppResponse(modalities: List<Modality>,
                                  appRequest: AppRequest,
                                  steps: List<Step>,
                                  sessionId: String): AppResponse {

        val results = steps.map { it.result }
        val faceResponse = getFaceResponseForEnrol(modalities, results)
        val fingerprintResponse = getFingerprintResponseForEnrol(modalities, results)

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
            else -> throw Throwable("All responses are null")
        }
    }

    private fun getFaceResponseForEnrol(modalities: List<Modality>, results: List<Step.Result?>): FaceEnrolResponse? {
        val index =  modalities.indexOf(Modality.FACE)
        return if (index > -1) {
            results[index] as FaceEnrolResponse
        } else {
            null
        }
    }

    private fun getFingerprintResponseForEnrol(modalities: List<Modality>, results: List<Step.Result?>): FingerprintEnrolResponse? {
        val index = modalities.indexOf(Modality.FINGER)
        return if (index > -1) {
            results[index] as FingerprintEnrolResponse
        } else {
            null
        }
    }

    private fun buildAppEnrolResponseForFingerprintAndFace(fingerprintResponse: FaceEnrolResponse,
                                                           faceResponse: FingerprintEnrolResponse) =
        AppEnrolResponse(fingerprintResponse.guid)

    private fun buildAppEnrolResponseForFingerprint(fingerprintResponse: FingerprintEnrolResponse) =
        AppEnrolResponse(fingerprintResponse.guid)

    private fun buildAppEnrolResponseForFace(faceResponse: FaceEnrolResponse) = AppEnrolResponse(faceResponse.guid)
}
