package com.simprints.id.domain.moduleapi.fingerprint.responses

import android.os.Parcelable
import com.simprints.id.orchestrator.steps.Step.Result
import com.simprints.moduleapi.fingerprint.responses.*

interface FingerprintResponse: Parcelable, Result {

    val type: FingerprintTypeResponse

    companion object {
        const val BUNDLE_KEY = "FingerprintResponseBundleKey"
    }

}

fun IFingerprintResponse.fromModuleApiToDomain(): FingerprintResponse =
    when (type) {
        IFingerprintResponseType.CAPTURE -> (this as IFingerprintCaptureResponse).fromModuleApiToDomain()
        IFingerprintResponseType.MATCH -> (this as IFingerprintMatchResponse).fromModuleApiToDomain()
        IFingerprintResponseType.REFUSAL -> (this as IFingerprintExitFormResponse).fromModuleApiToDomain()
        IFingerprintResponseType.ERROR -> (this as IFingerprintErrorResponse).fromModuleApiToDomain()
    }


enum class FingerprintTypeResponse {
    ENROL,
    MATCH,
    REFUSAL,
    ERROR
}
