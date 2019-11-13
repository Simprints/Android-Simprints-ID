package com.simprints.id.domain.moduleapi.fingerprint

import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintMatchRequest
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureSample

class FingerprintRequestFactoryImpl : FingerprintRequestFactory {

    override fun buildFingerprintCaptureRequest(projectId: String,
                                                userId: String,
                                                moduleId: String,
                                                metadata: String,
                                                prefs: PreferencesManager): FingerprintCaptureRequest =
        with(prefs) {
            FingerprintCaptureRequest(
                fingerprintsToCapture = fingerStatus.mapNotNull {
                    if (it.value)
                        it.key
                    else
                        null
                }
            )
        }

    override fun buildFingerprintMatchRequest(probeSamples: List<FingerprintCaptureSample>, query: PersonLocalDataSource.Query): FingerprintMatchRequest =
        FingerprintMatchRequest(probeSamples, query)
}
