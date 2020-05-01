package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.id.domain.moduleapi.app.responses.AppRefusalFormResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.app.responses.entities.RefusalFormAnswer
import com.simprints.id.domain.moduleapi.app.responses.entities.fromDomainToModuleApi
import com.simprints.id.domain.moduleapi.core.response.CoreExitFormResponse
import com.simprints.id.domain.moduleapi.core.response.CoreFaceExitFormResponse
import com.simprints.id.domain.moduleapi.core.response.CoreFingerprintExitFormResponse
import com.simprints.id.domain.moduleapi.core.response.FetchGUIDResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceExitFormResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintErrorResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintRefusalFormResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.toAppRefusalFormReason
import com.simprints.id.domain.moduleapi.fingerprint.responses.toAppErrorReason
import com.simprints.id.orchestrator.steps.Step

abstract class BaseAppResponseBuilder : AppResponseBuilder {

    fun getErrorOrRefusalResponseIfAny(steps: List<Step>): AppResponse? {

        val results = steps.map { it.getResult() }

        return when {
            results.any { it is CoreExitFormResponse } -> {
                buildAppExitFormResponse(results.find { it is CoreExitFormResponse } as CoreExitFormResponse)
            }
            results.any { it is CoreFingerprintExitFormResponse } -> {
                buildAppExitFormResponse(results.find { it is CoreFingerprintExitFormResponse } as CoreFingerprintExitFormResponse)
            }
            results.any { it is CoreFaceExitFormResponse } -> {
                buildAppExitFormResponse(results.find { it is CoreFaceExitFormResponse } as CoreFaceExitFormResponse)
            }
            results.any { it is FingerprintErrorResponse } -> {
                buildAppErrorResponse(results.find { it is FingerprintErrorResponse } as FingerprintErrorResponse)
            }
            results.any { it is FingerprintRefusalFormResponse } -> {
                buildAppRefusalResponse(results.find { it is FingerprintRefusalFormResponse } as FingerprintRefusalFormResponse)
            }
            results.any { it is FaceExitFormResponse } -> {
                buildAppExitFormResponse(results.find { it is FaceExitFormResponse } as FaceExitFormResponse)
            }
            results.any { it is FetchGUIDResponse } -> {
                buildAppErrorResponse(results.find { it is FetchGUIDResponse } as FetchGUIDResponse)
            }
            else -> {
                null
            }
        }
    }

    private fun buildAppExitFormResponse(coreExitFormResponse: CoreExitFormResponse) =
        AppRefusalFormResponse(RefusalFormAnswer(coreExitFormResponse.reason.fromDomainToModuleApi(),
            coreExitFormResponse.optionalText))

    private fun buildAppExitFormResponse(coreFingerprintExitFormResponse: CoreFingerprintExitFormResponse) =
        AppRefusalFormResponse(RefusalFormAnswer(coreFingerprintExitFormResponse.reason.fromDomainToModuleApi(),
            coreFingerprintExitFormResponse.optionalText))

    private fun buildAppExitFormResponse(coreFaceExitFormResponse: CoreFaceExitFormResponse) =
        AppRefusalFormResponse(RefusalFormAnswer(coreFaceExitFormResponse.reason.fromDomainToModuleApi(),
            coreFaceExitFormResponse.optionalText))

    private fun buildAppErrorResponse(fingerprintErrorResponse: FingerprintErrorResponse) =
        AppErrorResponse(fingerprintErrorResponse.fingerprintErrorReason.toAppErrorReason())

    private fun buildAppErrorResponse(fetchGUIDResponse: FetchGUIDResponse) =
        if (!fetchGUIDResponse.isGuidFound) {
            AppErrorResponse(AppErrorResponse.Reason.GUID_NOT_FOUND_ONLINE)
        } else {
            null
        }

    private fun buildAppRefusalResponse(fingerprintRefusalFormResponse: FingerprintRefusalFormResponse) =
        AppRefusalFormResponse(RefusalFormAnswer(
            fingerprintRefusalFormResponse.reason.toAppRefusalFormReason(),
            fingerprintRefusalFormResponse.optionalText))

    private fun buildAppExitFormResponse(faceExitFormResponse: FaceExitFormResponse) =
        AppRefusalFormResponse(RefusalFormAnswer(
            faceExitFormResponse.reason.toAppRefusalFormReason(),
            faceExitFormResponse.extra))
}
