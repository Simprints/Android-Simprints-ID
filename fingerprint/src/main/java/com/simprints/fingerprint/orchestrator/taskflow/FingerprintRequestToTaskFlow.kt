package com.simprints.fingerprint.orchestrator.taskflow

import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintEnrolRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintIdentifyRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintVerifyRequest
import com.simprints.fingerprint.exceptions.unexpected.request.InvalidRequestForFingerprintException

fun FingerprintRequest.toFingerprintTaskFlow(): FingerprintTaskFlow =
    when (this) {
        is FingerprintEnrolRequest -> EnrolTaskFlow()
        is FingerprintIdentifyRequest -> IdentifyTaskFlow()
        is FingerprintVerifyRequest -> VerifyTaskFlow()
        else -> throw InvalidRequestForFingerprintException("Could not get task flow for request")
    }.also { it.computeFlow(this) }
