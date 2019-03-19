package com.simprints.id.moduleapi

import com.simprints.id.FingerIdentifier
import com.simprints.id.FingerIdentifier.*
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.requests.EnrolRequest
import com.simprints.id.domain.requests.IdentifyRequest
import com.simprints.id.domain.requests.Request
import com.simprints.id.domain.requests.VerifyRequest
import com.simprints.moduleapi.fingerprint.requests.*
import kotlinx.android.parcel.Parcelize

object DomainToFingerprintAdapter {

    fun fromDomainToFingerprintRequest(appRequest: Request, prefs: PreferencesManager): IFingerprintRequest =
        when (appRequest) {
            is EnrolRequest -> fromDomainToFingerprintEnrolRequest(appRequest, prefs)
            is VerifyRequest -> fromDomainToFingerprintVerifyRequest(appRequest, prefs)
            is IdentifyRequest -> fromDomainToFingerprintIdentifyRequest(appRequest, prefs)
            else -> throw IllegalStateException("Invalid fingerprint request")
        }

    private fun fromDomainToFingerprintEnrolRequest(enrol: EnrolRequest, prefs: PreferencesManager): IFingerprintEnrolRequest =
        with(enrol) {
            FingerprintEnrolRequestImpl(
                projectId, userId, moduleId, metadata,
                prefs.language, prefs.fingerStatus.mapKeys { fromDomainToFingerprintFingerIdentifier(it.key) },
                prefs.nudgeMode,
                prefs.qualityThreshold,
                prefs.logoExists,
                prefs.organizationName,
                prefs.programName,
                prefs.vibrateMode)
        }

    private fun fromDomainToFingerprintVerifyRequest(verify: VerifyRequest, prefs: PreferencesManager): IFingerprintVerifyRequest =
        with(verify) {
            FingerprintVerifyRequestImpl(
                projectId, userId, moduleId, metadata, verifyGuid,
                prefs.language,
                prefs.fingerStatus.mapKeys { fromDomainToFingerprintFingerIdentifier(it.key) },
                prefs.nudgeMode,
                prefs.qualityThreshold,
                prefs.logoExists,
                prefs.organizationName,
                prefs.programName,
                prefs.vibrateMode)
        }

    private fun fromDomainToFingerprintIdentifyRequest(
        identify: IdentifyRequest,
        prefs: PreferencesManager,
        returnIdCount: Int = 10): IFingerprintIdentifyRequest =

        with(identify) {
            FingerprintIdentifyRequestImpl(
                projectId, userId, moduleId, metadata,
                prefs.language,
                prefs.fingerStatus.mapKeys { fromDomainToFingerprintFingerIdentifier(it.key) },
                prefs.nudgeMode,
                prefs.qualityThreshold,
                prefs.logoExists,
                prefs.organizationName,
                prefs.programName,
                prefs.vibrateMode,
                fromDomainToFingerprintMatchGroup(prefs.matchGroup),
                returnIdCount)
        }

    private fun fromDomainToFingerprintMatchGroup(matchGroup: GROUP): IMatchGroup =
        when (matchGroup) {
            GROUP.GLOBAL -> IMatchGroup.GLOBAL
            GROUP.USER -> IMatchGroup.USER
            GROUP.MODULE -> IMatchGroup.MODULE
        }

    private fun fromDomainToFingerprintFingerIdentifier(fingerIdentifier: FingerIdentifier): IFingerIdentifier =
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
private data class FingerprintEnrolRequestImpl(override val projectId: String,
                                               override val userId: String,
                                               override val moduleId: String,
                                               override val metadata: String,
                                               override val language: String,
                                               override val fingerStatus: Map<IFingerIdentifier, Boolean>,
                                               override val nudgeMode: Boolean,
                                               override val qualityThreshold: Int,
                                               override val logoExists: Boolean,
                                               override val programName: String,
                                               override val organizationName: String,
                                               override val vibrateMode: Boolean) : IFingerprintEnrolRequest

@Parcelize
private data class FingerprintIdentifyRequestImpl(override val projectId: String,
                                                  override val userId: String,
                                                  override val moduleId: String,
                                                  override val metadata: String,
                                                  override val language: String,
                                                  override val fingerStatus: Map<IFingerIdentifier, Boolean>,
                                                  override val nudgeMode: Boolean,
                                                  override val qualityThreshold: Int,
                                                  override val logoExists: Boolean,
                                                  override val programName: String,
                                                  override val organizationName: String,
                                                  override val vibrateMode: Boolean,
                                                  override val matchGroup: IMatchGroup,
                                                  override val returnIdCount: Int) : IFingerprintIdentifyRequest

@Parcelize
private data class FingerprintVerifyRequestImpl(override val projectId: String,
                                                override val userId: String,
                                                override val moduleId: String,
                                                override val metadata: String,
                                                override val verifyGuid: String,
                                                override val language: String,
                                                override val fingerStatus: Map<IFingerIdentifier, Boolean>,
                                                override val nudgeMode: Boolean,
                                                override val qualityThreshold: Int,
                                                override val logoExists: Boolean,
                                                override val programName: String,
                                                override val organizationName: String,
                                                override val vibrateMode: Boolean) : IFingerprintVerifyRequest
