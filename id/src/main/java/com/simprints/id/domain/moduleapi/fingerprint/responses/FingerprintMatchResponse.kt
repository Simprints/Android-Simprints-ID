package com.simprints.id.domain.moduleapi.fingerprint.responses

import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintMatchResult
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.fromModuleApiToDomain
import com.simprints.moduleapi.fingerprint.responses.IFingerprintMatchResponse
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class FingerprintMatchResponse(
    val result: List<FingerprintMatchResult>
) : FingerprintResponse {
    @IgnoredOnParcel override val type: FingerprintResponseType = FingerprintResponseType.MATCH
}

fun IFingerprintMatchResponse.fromModuleApiToDomain() = FingerprintMatchResponse(result.map { it.fromModuleApiToDomain() })
