package com.simprints.fingerprint.orchestrator.taskflow

import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.*
import com.simprints.fingerprint.exceptions.unexpected.request.InvalidRequestForFingerprintException

fun FingerprintRequest.toFingerprintTaskFlow(): FingerprintTaskFlow =
    when (this) {
        is FingerprintCaptureRequest -> CaptureTaskFlow(this)
        is FingerprintIdentifyRequest -> IdentifyTaskFlow(this)
        is FingerprintVerifyRequest -> VerifyTaskFlow(this)
        else -> throw InvalidRequestForFingerprintException("Could not get task flow for request")
    }
