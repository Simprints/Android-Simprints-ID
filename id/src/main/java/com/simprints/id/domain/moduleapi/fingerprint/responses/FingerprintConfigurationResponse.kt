package com.simprints.id.domain.moduleapi.fingerprint.responses

import com.simprints.moduleapi.fingerprint.responses.IFingerprintConfigurationResponse
import kotlinx.parcelize.Parcelize

@Parcelize
data class FingerprintConfigurationResponse(
    override val type: FingerprintResponseType = FingerprintResponseType.CONFIGURATION
) : FingerprintResponse

fun IFingerprintConfigurationResponse.fromModuleApiToDomain() = FingerprintConfigurationResponse()
