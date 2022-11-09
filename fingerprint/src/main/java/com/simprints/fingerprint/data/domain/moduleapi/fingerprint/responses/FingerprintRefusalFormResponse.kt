package com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses

import com.simprints.fingerprint.data.domain.refusal.RefusalFormReason
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * This class represents the response returned when a user submits a refusal form, when processing
 * processing a fingerprint request.
 *
 * @param reason the user's selected reason for refusing the biometric capture
 * @param extra any extra message included when submitting the refusal form
 */
@Parcelize
class FingerprintRefusalFormResponse(val reason: RefusalFormReason,
                                     val extra: String) : FingerprintResponse {
    @IgnoredOnParcel override val type: FingerprintResponseType = FingerprintResponseType.REFUSAL
}
