package com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
class FingerprintConfigurationResponse : FingerprintResponse {
    @IgnoredOnParcel override val type: FingerprintResponseType = FingerprintResponseType.CONFIGURATION
}
