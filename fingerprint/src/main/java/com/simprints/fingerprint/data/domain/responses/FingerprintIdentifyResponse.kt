package com.simprints.fingerprint.data.domain.responses

import android.os.Parcelable
import com.simprints.fingerprint.data.domain.matching.result.MatchingResult
import com.simprints.fingerprint.data.domain.matching.result.MatchingTier
import kotlinx.android.parcel.Parcelize

@Parcelize
class FingerprintIdentifyResponse(val identifications: List<MatchingResult>,
                                  val sessionId: String) : FingerprintResponse {

    interface IdentificationResult : Parcelable {
        val guid: String
        val confidence: Int
        val tier: MatchingTier
    }
}
