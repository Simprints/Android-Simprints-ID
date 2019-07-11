package com.simprints.fingerprint.orchestrator

import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintEnrolRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintIdentifyRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintVerifyRequest
import com.simprints.fingerprint.orchestrator.taskflow.FingerprintTaskFlow

fun FingerprintRequest.toFingerprintTaskFlow(): FingerprintTaskFlow =
    when (this) {
        is FingerprintEnrolRequest -> FingerprintTaskFlow.Enrol()
        is FingerprintIdentifyRequest -> FingerprintTaskFlow.Identify()
        is FingerprintVerifyRequest -> FingerprintTaskFlow.Verify()
        else -> throw Throwable("Woops") // TODO
    }.also { it.computeFlow(this) }
