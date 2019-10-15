package com.simprints.id.domain.moduleapi.fingerprint.responses

import com.simprints.moduleapi.fingerprint.responses.IFingerprintCaptureResponse
import com.simprints.moduleapi.fingerprint.responses.entities.IFingerprintCaptureResult
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintCaptureResponse(
    val captureResult: List<IFingerprintCaptureResult>,
    override val type: FingerprintResponseType = FingerprintResponseType.ENROL
) : FingerprintResponse

fun IFingerprintCaptureResponse.fromModuleApiToDomain() = FingerprintCaptureResponse(captureResult)
