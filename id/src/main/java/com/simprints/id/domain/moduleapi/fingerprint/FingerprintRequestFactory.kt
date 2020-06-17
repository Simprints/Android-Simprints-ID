package com.simprints.id.domain.moduleapi.fingerprint

import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintMatchRequest
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureSample

interface FingerprintRequestFactory {

    fun buildFingerprintCaptureRequest(prefs: PreferencesManager): FingerprintCaptureRequest

    fun buildFingerprintMatchRequest(probeSamples: List<FingerprintCaptureSample>,
                                     query: SubjectLocalDataSource.Query): FingerprintMatchRequest
}
