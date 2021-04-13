package com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses

import com.simprints.fingerprint.data.domain.refusal.RefusalFormReason
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
class FingerprintRefusalFormResponse(val reason: RefusalFormReason,
                                     val extra: String) : FingerprintResponse {
    @IgnoredOnParcel override val type: FingerprintResponseType = FingerprintResponseType.REFUSAL
}
