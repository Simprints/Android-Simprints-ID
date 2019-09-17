package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppIdentifyResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceIdentifyResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintIdentifyResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.toAppMatchResult
import com.simprints.id.orchestrator.steps.Step

class AppResponseBuilderForIdentify : AppResponseBuilder, BaseAppResponseBuilder() {

    override fun buildAppResponse(modalities: List<Modality>,
                                  appRequest: AppRequest,
                                  steps: List<Step>,
                                  sessionId: String): AppResponse {
        super.getErrorOrRefusalResponseIfAny(steps)?.let {
            return it
        }

        val results = steps.map { it.result }
        val faceResponse = getFaceResponseForIdentify(results)
        val fingerprintResponse = getFingerprintResponseForIdentify(results)

        return when {
            fingerprintResponse != null && faceResponse != null -> {
                buildAppIdentifyResponseForFaceAndFinger(faceResponse, fingerprintResponse, sessionId)
            }
            fingerprintResponse != null -> {
                buildAppIdentifyResponseForFingerprint(fingerprintResponse, sessionId)
            }
            faceResponse != null -> {
                buildAppIdentifyResponseForFace(faceResponse, sessionId)
            }
            else -> throw Throwable("All responses are null")
        }
    }

    private fun getFaceResponseForIdentify(results: List<Step.Result?>): FaceIdentifyResponse? =
        results.filterIsInstance(FaceIdentifyResponse::class.java).lastOrNull()

    private fun getFingerprintResponseForIdentify(results: List<Step.Result?>): FingerprintIdentifyResponse? =
        results.filterIsInstance(FingerprintIdentifyResponse::class.java).lastOrNull()

    private fun buildAppIdentifyResponseForFaceAndFinger(faceResponse: FaceIdentifyResponse,
                                                         fingerprintResponse: FingerprintIdentifyResponse,
                                                         sessionId: String) =
        AppIdentifyResponse(fingerprintResponse.identifications.map { it.toAppMatchResult() }, sessionId)

    private fun buildAppIdentifyResponseForFingerprint(fingerprintResponse: FingerprintIdentifyResponse,
                                                       sessionId: String) =
        AppIdentifyResponse(fingerprintResponse.identifications.map { it.toAppMatchResult() }, sessionId)

    private fun buildAppIdentifyResponseForFace(faceResponse: FaceIdentifyResponse, sessionId: String): AppIdentifyResponse {
        TODO("Not implemented yet")
    }
}
