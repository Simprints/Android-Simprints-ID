package com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses

import com.simprints.fingerprint.data.domain.matching.MatchResult
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * This class represents the response to a fingerprint match request
 *
 * @param result the list of matching candidates
 */
@Parcelize
class FingerprintMatchResponse(val result: List<MatchResult>) : FingerprintResponse {
    @IgnoredOnParcel override val type: FingerprintResponseType = FingerprintResponseType.MATCH
}
