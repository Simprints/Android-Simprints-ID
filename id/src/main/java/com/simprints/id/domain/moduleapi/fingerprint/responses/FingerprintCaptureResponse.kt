package com.simprints.id.domain.moduleapi.fingerprint.responses

import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureResult
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.fromModuleApiToDomain
import com.simprints.moduleapi.fingerprint.responses.IFingerprintCaptureResponse
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintCaptureResponse(
    val captureResult: List<FingerprintCaptureResult>,
    override val type: FingerprintTypeResponse = FingerprintTypeResponse.ENROL
) : FingerprintResponse

fun IFingerprintCaptureResponse.fromModuleApiToDomain() = FingerprintCaptureResponse(captureResult.map { it.fromModuleApiToDomain() })
