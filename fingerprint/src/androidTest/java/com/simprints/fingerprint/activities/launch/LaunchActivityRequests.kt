package com.simprints.fingerprint.activities.launch

import android.content.Intent
import com.simprints.moduleapi.fingerprint.requests.IFingerIdentifier
import com.simprints.moduleapi.fingerprint.requests.IFingerprintEnrolRequest
import com.simprints.moduleapi.fingerprint.requests.IFingerprintRequest
import com.simprints.moduleapi.fingerprint.requests.IFingerprintVerifyRequest
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class FingerprintEnrolRequest(
    override val projectId: String,
    override val userId: String,
    override val moduleId: String,
    override val metadata: String,
    override val language: String,
    override val fingerStatus: Map<IFingerIdentifier, Boolean>,
    override val logoExists: Boolean,
    override val programName: String,
    override val organizationName: String
) : IFingerprintEnrolRequest {

    fun toIntent() = Intent().also {
        it.putExtra(IFingerprintRequest.BUNDLE_KEY, this)
    }
}

@Parcelize
internal data class FingerprintVerifyRequest(
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
) : IFingerprintVerifyRequest {

    fun toIntent() = Intent().also {
        it.putExtra(IFingerprintRequest.BUNDLE_KEY, this)
    }
}
