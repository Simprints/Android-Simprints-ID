package com.simprints.id.domain.moduleapi.fingerprint

import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintMatchRequest

interface FingerprintRequestFactory {

    fun buildFingerprintCaptureRequest(projectId: String,
                                       userId: String,
                                       moduleId: String,
                                       metadata: String,
                                       prefs: PreferencesManager): FingerprintCaptureRequest

    fun buildFingerprintMatchRequest(): FingerprintMatchRequest
}
