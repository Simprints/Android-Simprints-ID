package com.simprints.fingerprint.orchestrator.taskflow

import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintConfigurationRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintMatchRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.exceptions.unexpected.request.InvalidRequestForFingerprintException

fun FingerprintRequest.toFingerprintTaskFlow(): FingerprintTaskFlow =
    when (this) {
        is FingerprintCaptureRequest -> CaptureTaskFlow(this)
        is FingerprintMatchRequest -> MatchTaskFlow(this)
        is FingerprintConfigurationRequest -> ConfigurationTaskFlow(this)
        else -> throw InvalidRequestForFingerprintException("Could not build task flow for request")
    }
