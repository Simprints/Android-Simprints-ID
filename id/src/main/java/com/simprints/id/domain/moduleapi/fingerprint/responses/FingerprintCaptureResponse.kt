package com.simprints.id.domain.moduleapi.fingerprint.responses

import com.simprints.moduleapi.fingerprint.responses.IFingerprint
import com.simprints.moduleapi.fingerprint.responses.IFingerprintCaptureResponse
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintCaptureResponse(
    val fingerprints: List<IFingerprint>,
    override val type: FingerprintTypeResponse = FingerprintTypeResponse.ENROL
) : FingerprintResponse

fun IFingerprintCaptureResponse.fromModuleApiToDomain() = FingerprintCaptureResponse(fingerprints)
