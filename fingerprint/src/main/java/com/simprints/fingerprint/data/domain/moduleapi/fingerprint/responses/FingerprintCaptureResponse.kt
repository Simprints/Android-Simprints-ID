package com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses

import com.simprints.fingerprint.data.domain.fingerprint.Fingerprint
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize


/**
 * This class represents the response to a fingerprint capture for a subject
 *
 *  @param fingerprints the list of captured fingerprints
 */
@Parcelize
class FingerprintCaptureResponse(val fingerprints: List<Fingerprint>): FingerprintResponse {
    @IgnoredOnParcel override val type: FingerprintResponseType = FingerprintResponseType.CAPTURE
}
