package com.simprints.id.moduleapi

import com.simprints.id.FingerIdentifier
import com.simprints.id.FingerIdentifier.*
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.requests.EnrolRequest
import com.simprints.id.domain.requests.IdentifyRequest
import com.simprints.id.domain.requests.Request
import com.simprints.id.domain.requests.VerifyRequest
import com.simprints.moduleapi.fingerprint.*
import kotlinx.android.parcel.Parcelize

object FingerprintAdapter {

    fun toFingerprintRequest(appRequest: Request, prefs: PreferencesManager): IFingerprintRequest =
        when (appRequest) {
            is EnrolRequest -> toFingerprintEnrolRequest(appRequest, prefs)
            is VerifyRequest -> toFingerprintVerifyRequest(appRequest, prefs)
            is IdentifyRequest -> toFingerprintIdentifyRequest(appRequest, prefs)
            else -> throw IllegalStateException("Invalid fingerprint request")
        }

    private fun toFingerprintEnrolRequest(enrol: EnrolRequest, prefs: PreferencesManager): IFingerprintEnrolRequest =
        with(enrol) {
            FingerprintEnrollRequest(
                projectId, userId, moduleId, metadata,
                prefs.language, prefs.fingerStatus.mapKeys { toFingerprintFingerIdentifier(it.key) }, prefs.nudgeMode, prefs.qualityThreshold)
        }

    private fun toFingerprintVerifyRequest(verify: VerifyRequest, prefs: PreferencesManager): IFingerprintVerifyRequest =
        with(verify) {
            FingerprintVerifyRequest(
                projectId, userId, moduleId, metadata, verifyGuid,
                prefs.language, prefs.fingerStatus.mapKeys { toFingerprintFingerIdentifier(it.key) }, prefs.nudgeMode, prefs.qualityThreshold)
        }

    private fun toFingerprintIdentifyRequest(identify: IdentifyRequest, prefs: PreferencesManager): IFingerprintIdentifyRequest =
        with(identify) {
            FingerprintIdentifyRequest(
                projectId, userId, moduleId, metadata,
                prefs.language, prefs.fingerStatus.mapKeys { toFingerprintFingerIdentifier(it.key) }, prefs.nudgeMode, prefs.qualityThreshold)
        }

    private fun toFingerprintFingerIdentifier(fingerIdentifier: FingerIdentifier): IFingerIdentifier =
        when (fingerIdentifier) {
            RIGHT_5TH_FINGER -> IFingerIdentifier.RIGHT_5TH_FINGER
            RIGHT_4TH_FINGER -> IFingerIdentifier.RIGHT_4TH_FINGER
            RIGHT_3RD_FINGER -> IFingerIdentifier.RIGHT_3RD_FINGER
            RIGHT_INDEX_FINGER -> IFingerIdentifier.RIGHT_INDEX_FINGER
            RIGHT_THUMB -> IFingerIdentifier.RIGHT_THUMB
            LEFT_THUMB -> IFingerIdentifier.LEFT_THUMB
            LEFT_INDEX_FINGER -> IFingerIdentifier.LEFT_INDEX_FINGER
            LEFT_3RD_FINGER -> IFingerIdentifier.LEFT_3RD_FINGER
            LEFT_4TH_FINGER -> IFingerIdentifier.LEFT_4TH_FINGER
            LEFT_5TH_FINGER -> IFingerIdentifier.LEFT_5TH_FINGER
        }
}

@Parcelize
private data class FingerprintEnrollRequest(override val projectId: String,
                                            override val userId: String,
                                            override val moduleId: String,
                                            override val metadata: String,
                                            override val language: String,
                                            override val fingerStatus: Map<IFingerIdentifier, Boolean>,
                                            override val nudgeMode: Boolean,
                                            override val qualityThreshold: Int) : IFingerprintEnrolRequest

@Parcelize
private data class FingerprintIdentifyRequest(override val projectId: String,
                                              override val userId: String,
                                              override val moduleId: String,
                                              override val metadata: String,
                                              override val language: String,
                                              override val fingerStatus: Map<IFingerIdentifier, Boolean>,
                                              override val nudgeMode: Boolean,
                                              override val qualityThreshold: Int) : IFingerprintIdentifyRequest

@Parcelize
private data class FingerprintVerifyRequest(override val projectId: String,
                                            override val userId: String,
                                            override val moduleId: String,
                                            override val metadata: String,
                                            override val verifyGuid: String,
                                            override val language: String,
                                            override val fingerStatus: Map<IFingerIdentifier, Boolean>,
                                            override val nudgeMode: Boolean,
                                            override val qualityThreshold: Int) : IFingerprintVerifyRequest
