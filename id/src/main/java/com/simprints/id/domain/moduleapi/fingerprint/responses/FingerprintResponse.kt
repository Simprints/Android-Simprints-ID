package com.simprints.id.domain.moduleapi.fingerprint.responses

import android.os.Parcelable
import com.simprints.id.orchestrator.steps.Step.Result
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports
import com.simprints.moduleapi.fingerprint.responses.*

interface FingerprintResponse : Parcelable, Result {

    val type: FingerprintResponseType

    companion object {
        const val BUNDLE_KEY = "FingerprintResponseBundleKey"
    }

}

@ExcludedFromGeneratedTestCoverageReports("Deprecated code")
fun IFingerprintResponse.fromModuleApiToDomain(): FingerprintResponse? = when (type) {
    IFingerprintResponseType.CAPTURE -> (this as IFingerprintCaptureResponse).fromModuleApiToDomain()
    IFingerprintResponseType.MATCH -> (this as IFingerprintMatchResponse).fromModuleApiToDomain()
    IFingerprintResponseType.REFUSAL -> (this as IFingerprintExitFormResponse).fromModuleApiToDomain()
    IFingerprintResponseType.ERROR -> (this as IFingerprintErrorResponse).fromModuleApiToDomain()
    else -> null
}

enum class FingerprintResponseType {
    ENROL,
    MATCH,
    REFUSAL,
    ERROR,
}
