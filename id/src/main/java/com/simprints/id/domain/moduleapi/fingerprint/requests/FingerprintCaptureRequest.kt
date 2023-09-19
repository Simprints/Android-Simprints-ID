package com.simprints.id.domain.moduleapi.fingerprint.requests

import com.simprints.id.domain.moduleapi.fingerprint.models.fromDomainToModuleApi
import com.simprints.infra.config.domain.models.Finger
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import com.simprints.moduleapi.fingerprint.requests.IFingerprintCaptureRequest
import kotlinx.parcelize.Parcelize

//Do not change the order of the parameters. Parcelize is not able to marshall correctly if type is the 2nd param
@Parcelize
data class FingerprintCaptureRequest(
    val fingerprintsToCapture: List<Finger>,
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
