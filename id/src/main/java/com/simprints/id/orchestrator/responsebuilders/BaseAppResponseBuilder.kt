package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.id.domain.moduleapi.app.responses.AppRefusalFormResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.app.responses.entities.RefusalFormAnswer
import com.simprints.id.domain.moduleapi.app.responses.entities.fromDomainToModuleApi
import com.simprints.id.domain.moduleapi.face.responses.FaceErrorResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceExitFormResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintErrorResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintRefusalFormResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.toAppRefusalFormReason
import com.simprints.id.domain.moduleapi.fingerprint.responses.toAppErrorReason
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.response.ExitFormResponse
import com.simprints.id.orchestrator.steps.core.response.FetchGUIDResponse
import com.simprints.id.orchestrator.steps.core.response.SetupResponse

abstract class BaseAppResponseBuilder : AppResponseBuilder {

    fun getErrorOrRefusalResponseIfAny(steps: List<Step>): AppResponse? {

        val results = steps.map { it.getResult() }

        return when {
            results.any { it is ExitFormResponse } -> {
                buildAppExitFormResponse(results.find { it is ExitFormResponse } as ExitFormResponse)
            }
            results.any { it is FaceExitFormResponse} -> {
                buildAppExitFormResponse(results.find { it is FaceExitFormResponse } as FaceExitFormResponse)
            }
            results.any { it is FingerprintRefusalFormResponse } -> {
                buildAppRefusalResponse(results.find { it is FingerprintRefusalFormResponse } as FingerprintRefusalFormResponse)
            }
            results.any { it is FingerprintErrorResponse } -> {
                buildAppErrorResponse(results.find { it is FingerprintErrorResponse } as FingerprintErrorResponse)
            }
            results.any { it is FaceErrorResponse } -> {
                buildAppErrorResponse(results.find { it is FaceErrorResponse } as FaceErrorResponse)
            }
            results.any { it is FetchGUIDResponse } -> {
                buildAppErrorResponse(results.find { it is FetchGUIDResponse } as FetchGUIDResponse)
            }
            results.any { it is SetupResponse } -> {
                buildAppErrorResponse(results.find { it is SetupResponse } as SetupResponse)
            }
            else -> {
                null
            }
        }
    }

    private fun buildAppExitFormResponse(exitFormResponse: ExitFormResponse) =
        AppRefusalFormResponse(RefusalFormAnswer(exitFormResponse.reason.fromDomainToModuleApi(), exitFormResponse.optionalText))

    private fun buildAppErrorResponse(fingerprintErrorResponse: FingerprintErrorResponse) =
        AppErrorResponse(fingerprintErrorResponse.fingerprintErrorReason.toAppErrorReason())

    private fun buildAppErrorResponse(faceErrorResponse: FaceErrorResponse) =
        AppErrorResponse(faceErrorResponse.faceErrorReason.toAppErrorReason())

    private fun buildAppErrorResponse(fetchGUIDResponse: FetchGUIDResponse) =
        if (!fetchGUIDResponse.isGuidFound) {
            if (fetchGUIDResponse.wasOnline) {
                AppErrorResponse(AppErrorResponse.Reason.GUID_NOT_FOUND_ONLINE)
            } else {
                AppErrorResponse(AppErrorResponse.Reason.GUID_NOT_FOUND_OFFLINE)
            }
        } else {
            null
        }

    private fun buildAppErrorResponse(setupResponse: SetupResponse) =
        if(!setupResponse.isSetupComplete) {
            AppErrorResponse(AppErrorResponse.Reason.LOGIN_NOT_COMPLETE)
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
