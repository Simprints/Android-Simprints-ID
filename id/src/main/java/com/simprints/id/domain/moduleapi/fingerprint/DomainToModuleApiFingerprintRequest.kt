package com.simprints.id.domain.moduleapi.fingerprint

import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintIdentifyRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintVerifyRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.entities.FingerprintFingerIdentifier
import com.simprints.id.domain.moduleapi.fingerprint.requests.entities.FingerprintFingerIdentifier.*
import com.simprints.id.domain.moduleapi.fingerprint.requests.entities.FingerprintMatchGroup
import com.simprints.id.domain.moduleapi.fingerprint.requests.entities.FingerprintMatchGroup.*
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import com.simprints.moduleapi.fingerprint.requests.*
import kotlinx.android.parcel.Parcelize

object DomainToModuleApiFingerprintRequest {

    fun fromDomainToModuleApiFingerprintRequest(fingerprintRequest: FingerprintRequest): IFingerprintRequest =
        when (fingerprintRequest) {
            is FingerprintCaptureRequest -> fromDomainToModuleApiFingerprintCaptureRequest(fingerprintRequest)
            is FingerprintVerifyRequest -> fromDomainToModuleApiFingerprintVerifyRequest(fingerprintRequest)
            is FingerprintIdentifyRequest -> fromDomainToModuleApiFingerprintIdentifyRequest(fingerprintRequest)
            else -> throw IllegalStateException("Invalid fingerprint request")
        }

    private fun fromDomainToModuleApiFingerprintCaptureRequest(captureRequest: FingerprintCaptureRequest): IFingerprintCaptureRequest =
        with(captureRequest) {
            FingerprintCaptureRequestImpl(
                projectId, userId, moduleId, metadata, language, fingerStatus.mapKeys { fromDomainToFingerprintFingerIdentifier(it.key) },
                logoExists,
                organizationName,
                programName,
                activityTitle
            )
        }

    private fun fromDomainToModuleApiFingerprintVerifyRequest(verifyRequest: FingerprintVerifyRequest): IFingerprintVerifyRequest =
        with(verifyRequest) {
            FingerprintVerifyRequestImpl(
                projectId, userId, moduleId, metadata, language, fingerStatus.mapKeys { fromDomainToFingerprintFingerIdentifier(it.key) },
                logoExists,
                organizationName,
                programName,
                verifyGuid)
        }

    private fun fromDomainToModuleApiFingerprintIdentifyRequest(identifyRequest: FingerprintIdentifyRequest): IFingerprintIdentifyRequest =

        with(identifyRequest) {
            FingerprintIdentifyRequestImpl(
                projectId, userId, moduleId, metadata, language,
                fingerStatus.mapKeys { fromDomainToFingerprintFingerIdentifier(it.key) },
                logoExists,
                organizationName,
                programName,
                fromDomainToFingerprintMatchGroup(matchGroup),
                returnIdCount)
        }

    private fun fromDomainToFingerprintMatchGroup(matchGroup: FingerprintMatchGroup): IMatchGroup =
        when (matchGroup) {
            GLOBAL -> IMatchGroup.GLOBAL
            USER -> IMatchGroup.USER
            MODULE -> IMatchGroup.MODULE
        }

    private fun fromDomainToFingerprintFingerIdentifier(fingerIdentifier: FingerprintFingerIdentifier): IFingerIdentifier =
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
private data class FingerprintCaptureRequestImpl(
    override val projectId: String,
    override val userId: String,
    override val moduleId: String,
    override val metadata: String,
    override val language: String,
    override val fingerStatus: Map<IFingerIdentifier, Boolean>,
    override val logoExists: Boolean,
    override val programName: String,
    override val organizationName: String,
    override val activityTitle: String
) : IFingerprintCaptureRequest

@Parcelize
private data class FingerprintIdentifyRequestImpl(
    override val projectId: String,
    override val userId: String,
    override val moduleId: String,
    override val metadata: String,
    override val language: String,
    override val fingerStatus: Map<IFingerIdentifier, Boolean>,
    override val logoExists: Boolean,
    override val programName: String,
    override val organizationName: String,
    override val matchGroup: IMatchGroup,
    override val returnIdCount: Int
) : IFingerprintIdentifyRequest

@Parcelize
private data class FingerprintVerifyRequestImpl(
    override val projectId: String,
    override val userId: String,
    override val moduleId: String,
    override val metadata: String,
    override val language: String,
    override val fingerStatus: Map<IFingerIdentifier, Boolean>,
    override val logoExists: Boolean,
    override val programName: String,
    override val organizationName: String,
    override val verifyGuid: String
) : IFingerprintVerifyRequest
