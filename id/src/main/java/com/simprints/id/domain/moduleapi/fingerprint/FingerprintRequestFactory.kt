package com.simprints.id.domain.moduleapi.fingerprint

import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintRequest

interface FingerprintRequestFactory {
    fun buildFingerprintRequest(appRequest: AppRequest, prefs: PreferencesManager): FingerprintRequest
}
