package com.simprints.id.domain.moduleapi.fingerprint

import com.simprints.id.data.db.person.domain.FingerIdentifier
import com.simprints.id.data.db.person.domain.FingerIdentifier.*
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintIdentifyRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintVerifyRequest
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
                projectId,
                userId,
                moduleId,
                metadata,
                language,
                fingerStatus.mapKeys { buildFingerprintFingerIdentifier(it.key) },
                logoExists,
                organizationName,
                programName,
                fingerStatus.mapNotNull {
                    if (it.value)
                        buildFingerprintFingerIdentifier(it.key)
                    else
                        null
                }
            )
        }

    override fun buildFingerprintVerifyRequest(projectId: String,
                                               userId: String,
                                               moduleId: String,
                                               metadata: String,
                                               verifyGuid: String,
                                               prefs: PreferencesManager): FingerprintVerifyRequest =
        with(prefs) {
            FingerprintVerifyRequest(
                projectId, userId, moduleId, metadata,
                language,
                fingerStatus.mapKeys { buildFingerprintFingerIdentifier(it.key) },
                logoExists,
                organizationName,
                programName,
                verifyGuid)
        }

    override fun buildFingerprintIdentifyRequest(projectId: String,
                                                 userId: String,
                                                 moduleId: String,
                                                 metadata: String,
                                                 prefs: PreferencesManager,
                                                 returnIdCount: Int): FingerprintIdentifyRequest =

        with(prefs) {
            FingerprintIdentifyRequest(
                projectId, userId, moduleId, metadata,
                language,
                fingerStatus.mapKeys { buildFingerprintFingerIdentifier(it.key) },
                logoExists,
                organizationName,
                programName,
                buildFingerprintMatchGroup(matchGroup),
                returnIdCount)
        }

    private fun buildFingerprintMatchGroup(matchGroup: GROUP): FingerprintMatchGroup =
        when (matchGroup) {
            GROUP.GLOBAL -> FingerprintMatchGroup.GLOBAL
            GROUP.USER -> FingerprintMatchGroup.USER
            GROUP.MODULE -> FingerprintMatchGroup.MODULE
        }

    private fun buildFingerprintFingerIdentifier(fingerIdentifier: FingerIdentifier): FingerprintFingerIdentifier =
        when (fingerIdentifier) {
            RIGHT_5TH_FINGER -> FingerprintFingerIdentifier.RIGHT_5TH_FINGER
            RIGHT_4TH_FINGER -> FingerprintFingerIdentifier.RIGHT_4TH_FINGER
            RIGHT_3RD_FINGER -> FingerprintFingerIdentifier.RIGHT_3RD_FINGER
            RIGHT_INDEX_FINGER -> FingerprintFingerIdentifier.RIGHT_INDEX_FINGER
            RIGHT_THUMB -> FingerprintFingerIdentifier.RIGHT_THUMB
            LEFT_THUMB -> FingerprintFingerIdentifier.LEFT_THUMB
            LEFT_INDEX_FINGER -> FingerprintFingerIdentifier.LEFT_INDEX_FINGER
            LEFT_3RD_FINGER -> FingerprintFingerIdentifier.LEFT_3RD_FINGER
            LEFT_4TH_FINGER -> FingerprintFingerIdentifier.LEFT_4TH_FINGER
            LEFT_5TH_FINGER -> FingerprintFingerIdentifier.LEFT_5TH_FINGER
        }
}
