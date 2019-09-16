package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.data.exitform.toAppRefusalFormReason
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.id.domain.moduleapi.app.responses.AppRefusalFormResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.app.responses.entities.RefusalFormAnswer
import com.simprints.id.domain.moduleapi.core.response.CoreExitFormResponse
import com.simprints.id.domain.moduleapi.core.response.FingerprintExitFormResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintErrorResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintRefusalFormResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.toAppRefusalFormReason
import com.simprints.id.domain.moduleapi.fingerprint.responses.toAppErrorReason
import com.simprints.id.orchestrator.steps.Step

abstract class BaseAppResponseBuilder : AppResponseBuilder {

    fun getErrorOrRefusalResponseIfAny(steps: List<Step>): AppResponse? {

        val results = steps.map { it.result }

        return when {
            results.any { it is CoreExitFormResponse } -> {
                buildAppExitFormResponse(results.find { it is CoreExitFormResponse } as CoreExitFormResponse)
            }
            results.any { it is FingerprintExitFormResponse } -> {
                buildAppExitFormResponse(results.find { it is FingerprintExitFormResponse } as FingerprintExitFormResponse)
            }
            results.any { it is FingerprintErrorResponse } -> {
                buildAppErrorResponse(results.find { it is FingerprintErrorResponse } as FingerprintErrorResponse)
            }
            results.any { it is FingerprintRefusalFormResponse } -> {
                buildAppRefusalResponse(results.find { it is FingerprintRefusalFormResponse } as FingerprintRefusalFormResponse)
            }
            else -> {
                null
            }
        }
    }

    private fun buildAppExitFormResponse(fingerprintExitFormResponse: FingerprintExitFormResponse) =
        AppRefusalFormResponse(RefusalFormAnswer(fingerprintExitFormResponse.reason.toAppRefusalFormReason(),
            fingerprintExitFormResponse.optionalText))

    private fun buildAppExitFormResponse(coreExitFormResponse: CoreExitFormResponse) =
        AppRefusalFormResponse(RefusalFormAnswer(coreExitFormResponse.reason.toAppRefusalFormReason(),
            coreExitFormResponse.optionalText))

    private fun buildAppErrorResponse(fingerprintErrorResponse: FingerprintErrorResponse) =
        AppErrorResponse(fingerprintErrorResponse.fingerprintErrorReason.toAppErrorReason())

    private fun buildAppRefusalResponse(fingerprintRefusalFormResponse: FingerprintRefusalFormResponse) =
        AppRefusalFormResponse(RefusalFormAnswer(
            fingerprintRefusalFormResponse.reason.toAppRefusalFormReason(),
            fingerprintRefusalFormResponse.optionalText))
}
