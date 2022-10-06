package com.simprints.id.domain.moduleapi.fingerprint

import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintConfigurationRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintMatchRequest
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureSample
import com.simprints.infra.config.domain.models.Finger
import com.simprints.infra.enrolment.records.domain.models.SubjectQuery

interface FingerprintRequestFactory {

    fun buildFingerprintCaptureRequest(fingersToCapture: List<Finger>): FingerprintCaptureRequest

    fun buildFingerprintMatchRequest(
        probeSamples: List<FingerprintCaptureSample>,
        query: SubjectQuery
    ): FingerprintMatchRequest

    fun buildFingerprintConfigurationRequest(): FingerprintConfigurationRequest

}
