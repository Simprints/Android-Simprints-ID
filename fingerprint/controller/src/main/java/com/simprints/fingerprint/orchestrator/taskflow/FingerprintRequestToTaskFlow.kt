package com.simprints.fingerprint.orchestrator.taskflow

import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.exceptions.unexpected.request.InvalidRequestForFingerprintException

fun FingerprintRequest.toFingerprintTaskFlow(): FingerprintTaskFlow =
    when (this) {
        is FingerprintCaptureRequest -> CaptureTaskFlow(this)
        else -> throw InvalidRequestForFingerprintException("Could not build task flow for request")
    }
