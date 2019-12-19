package com.simprints.id.domain.moduleapi.fingerprint.requests

import com.simprints.id.data.db.person.domain.FingerIdentifier
import com.simprints.id.data.db.person.domain.fromDomainToModuleApi
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import com.simprints.moduleapi.fingerprint.requests.IFingerprintCaptureRequest
import kotlinx.android.parcel.Parcelize

//Do not change the order of the parameters. Parcelize is not able to marshall correctly if type is the 2nd param
@Parcelize
data class FingerprintCaptureRequest(
    override val type: FingerprintRequestType = FingerprintRequestType.CAPTURE,
    val fingerprintsToCapture: List<FingerIdentifier>
) : FingerprintRequest

fun FingerprintCaptureRequest.fromDomainToModuleApi(): IFingerprintCaptureRequest =
    FingerprintCaptureRequestImpl(
        fingerprintsToCapture.map { it.fromDomainToModuleApi() }
    )


@Parcelize
private data class FingerprintCaptureRequestImpl(
    override val fingerprintsToCapture: List<IFingerIdentifier>
) : IFingerprintCaptureRequest
