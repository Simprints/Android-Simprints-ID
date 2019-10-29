package com.simprints.id.domain.moduleapi.fingerprint.requests

import com.simprints.id.data.db.person.domain.FingerIdentifier
import com.simprints.id.data.db.person.domain.fromDomainToModuleApi
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import com.simprints.moduleapi.fingerprint.requests.IFingerprintCaptureRequest
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintCaptureRequest(
    val fingerprintsToCapture: List<FingerIdentifier>,
    override val type: FingerprintRequestType = FingerprintRequestType.CAPTURE
) : FingerprintRequest

fun FingerprintCaptureRequest.fromDomainToModuleApi(): IFingerprintCaptureRequest =
    FingerprintCaptureRequestImpl(
        fingerprintsToCapture.map { it.fromDomainToModuleApi() }
    )


@Parcelize
private data class FingerprintCaptureRequestImpl(
    override val fingerprintsToCapture: List<IFingerIdentifier>
) : IFingerprintCaptureRequest
