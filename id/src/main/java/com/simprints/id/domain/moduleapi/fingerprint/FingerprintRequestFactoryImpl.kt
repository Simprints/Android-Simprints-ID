package com.simprints.id.domain.moduleapi.fingerprint

import com.simprints.id.data.db.person.domain.FingerIdentifier
import com.simprints.id.data.db.person.domain.FingerIdentifier.*
import com.simprints.id.data.db.person.domain.FingerprintSample
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.moduleapi.fingerprint.requests.*
import com.simprints.id.domain.moduleapi.fingerprint.requests.entities.FingerprintFingerIdentifier
import com.simprints.id.domain.moduleapi.fingerprint.requests.entities.FingerprintMatchGroup


class FingerprintRequestFactoryImpl : FingerprintRequestFactory {

    override fun buildFingerprintCaptureRequest(projectId: String,
                                                userId: String,
                                                moduleId: String,
                                                metadata: String,
                                                prefs: PreferencesManager): FingerprintCaptureRequest =
        with(prefs) {
            FingerprintCaptureRequest(
                fingerStatus.mapNotNull {
                    if (it.value)
                        buildFingerprintFingerIdentifier(it.key)
                    else
                        null
                }
            )
        }

    override fun buildFingerprintMatchRequest(probeSamples: List<FingerprintSample>, query: PersonLocalDataSource.Query): FingerprintMatchRequest =
        FingerprintMatchRequest(probeSamples, query)
}
