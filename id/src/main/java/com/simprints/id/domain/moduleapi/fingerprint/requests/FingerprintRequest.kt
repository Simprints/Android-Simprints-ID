package com.simprints.id.domain.moduleapi.fingerprint.requests

import android.os.Parcelable
import com.simprints.id.orchestrator.steps.Step.Request

interface FingerprintRequest: Parcelable, Request {
    val type: FingerprintRequestType
}

enum class FingerprintRequestType {
    CAPTURE,
    MATCH
}


fun FingerprintRequest.fromDomainToModuleApi() =
    when (this.type) {
        FingerprintRequestType.CAPTURE -> (this as FingerprintCaptureRequest).fromDomainToModuleApi()
        FingerprintRequestType.MATCH -> (this as FingerprintMatchRequest).fromDomainToModuleApi()
    }
