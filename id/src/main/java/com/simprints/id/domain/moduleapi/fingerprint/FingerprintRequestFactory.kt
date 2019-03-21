package com.simprints.id.domain.moduleapi.fingerprint

import com.simprints.id.FingerIdentifier
import com.simprints.id.FingerIdentifier.*
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintEnrolRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintIdentifyRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintVerifyRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.entities.FingerprintFingerIdentifier
import com.simprints.id.domain.moduleapi.fingerprint.requests.entities.FingerprintMatchGroup


object FingerprintRequestFactory {

    fun buildFingerprintRequest(appRequest: AppRequest, prefs: PreferencesManager): FingerprintRequest =
        when (appRequest) {
            is AppEnrolRequest -> buildFingerprintEnrolRequest(appRequest, prefs)
            is AppVerifyRequest -> buildFingerprintVerifyRequest(appRequest, prefs)
            is AppIdentifyRequest -> buildFingerprintIdentifyRequest(appRequest, prefs)
            else -> throw IllegalStateException("Invalid fingerprint request")
        }

    private fun buildFingerprintEnrolRequest(enrol: AppEnrolRequest, prefs: PreferencesManager): FingerprintEnrolRequest =
        with(enrol) {
            FingerprintEnrolRequest(
                projectId, userId, moduleId, metadata,
                prefs.language,
                prefs.fingerStatus.mapKeys { buildFingerprintFingerIdentifier(it.key) },
                prefs.nudgeMode,
                prefs.qualityThreshold,
                prefs.logoExists,
                prefs.organizationName,
                prefs.programName,
                prefs.vibrateMode)
        }

    private fun buildFingerprintVerifyRequest(verify: AppVerifyRequest, prefs: PreferencesManager): FingerprintVerifyRequest =
        with(verify) {
            FingerprintVerifyRequest(
                projectId, userId, moduleId, metadata,
                prefs.language,
                prefs.fingerStatus.mapKeys { buildFingerprintFingerIdentifier(it.key) },
                prefs.nudgeMode,
                prefs.qualityThreshold,
                prefs.logoExists,
                prefs.organizationName,
                prefs.programName,
                prefs.vibrateMode,
                verifyGuid)
        }

    private fun buildFingerprintIdentifyRequest(
        identify: AppIdentifyRequest,
        prefs: PreferencesManager,
        returnIdCount: Int = 10): FingerprintIdentifyRequest =

        with(identify) {
            FingerprintIdentifyRequest(
                projectId, userId, moduleId, metadata,
                prefs.language,
                prefs.fingerStatus.mapKeys { buildFingerprintFingerIdentifier(it.key) },
                prefs.nudgeMode,
                prefs.qualityThreshold,
                prefs.logoExists,
                prefs.organizationName,
                prefs.programName,
                prefs.vibrateMode,
                buildFingerprintMatchGroup(prefs.matchGroup),
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
