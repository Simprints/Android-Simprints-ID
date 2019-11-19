package com.simprints.id.domain.moduleapi.fingerprint

import com.simprints.id.data.db.person.domain.FingerprintSample
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintMatchRequest

interface FingerprintRequestFactory {

    fun buildFingerprintCaptureRequest(prefs: PreferencesManager): FingerprintCaptureRequest

    fun buildFingerprintMatchRequest(probeSamples: List<FingerprintSample>,
                                     query: PersonLocalDataSource.Query): FingerprintMatchRequest
}
