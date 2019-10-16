package com.simprints.id.domain.moduleapi.fingerprint.responses

import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureResult
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.fromModuleApiToDomain
import com.simprints.moduleapi.fingerprint.responses.IFingerprintCaptureResponse
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintCaptureResponse(
    override val type: FingerprintResponseType = FingerprintResponseType.ENROL,
    val captureResult: List<FingerprintCaptureResult>
) : FingerprintResponse

fun IFingerprintCaptureResponse.fromModuleApiToDomain() = FingerprintCaptureResponse(
    captureResult = captureResult.map { it.fromModuleApiToDomain() }
)
