package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppIdentifyResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceIdentifyResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.toAppMatchResult
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintIdentifyResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.toAppMatchResult
import com.simprints.id.orchestrator.steps.Step

class AppResponseBuilderForIdentify : AppResponseBuilder {

    override fun buildAppResponse(modalities: List<Modality>, appRequest: AppRequest, steps: List<Step>, sessionId: String): AppResponse {

        val results = steps.map { it.result }
        val faceResponse = getFaceResponseForIdentify(modalities, results)
        val fingerprintResponse = getFingerprintResponseForIdentify(modalities, results)

        return when {
            fingerprintResponse != null && faceResponse != null -> {
                buildAppIdentifyResponseForFaceAndFinger(faceResponse, fingerprintResponse, sessionId)
            }
            fingerprintResponse != null -> {
                buildAppEnrolResponseForFingerprint(fingerprintResponse, sessionId)
            }
            faceResponse != null -> {
                buildAppEnrolResponseForFace(faceResponse, sessionId)
            }
            else -> throw Throwable("All responses are null")
        }
    }

    private fun getFaceResponseForIdentify(modalities: List<Modality>, results: List<Step.Result?>): FaceIdentifyResponse? {
        val index =  modalities.indexOf(Modality.FACE)
        return if (index > -1) {
            results[index] as FaceIdentifyResponse
        } else {
            null
        }
    }

    private fun getFingerprintResponseForIdentify(modalities: List<Modality>, results: List<Step.Result?>): FingerprintIdentifyResponse? {
        val index =  modalities.indexOf(Modality.FACE)
        return if (index > -1) {
            results[index] as FingerprintIdentifyResponse
        } else {
            null
        }
    }

    private fun buildAppIdentifyResponseForFaceAndFinger(faceResponse: FaceIdentifyResponse,
                                                         fingerprintResponse: FingerprintIdentifyResponse,
                                                         sessionId: String) =
        AppIdentifyResponse(fingerprintResponse.identifications.map { it.toAppMatchResult() }, sessionId)

    private fun buildAppEnrolResponseForFingerprint(fingerprintResponse: FingerprintIdentifyResponse,
                                                    sessionId: String) =
        AppIdentifyResponse(fingerprintResponse.identifications.map { it.toAppMatchResult() }, sessionId)

    private fun buildAppEnrolResponseForFace(faceResponse: FaceIdentifyResponse, sessionId: String) =
        AppIdentifyResponse(faceResponse.identifications.map { it.toAppMatchResult() }, sessionId)

}
