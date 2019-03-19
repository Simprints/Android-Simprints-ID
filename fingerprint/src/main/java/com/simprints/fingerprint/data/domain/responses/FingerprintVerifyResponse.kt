package com.simprints.fingerprint.data.domain.responses

import com.simprints.fingerprint.data.domain.matching.result.MatchingTier
import kotlinx.android.parcel.Parcelize

@Parcelize
class FingerprintVerifyResponse(val guid: String,
                                val confidence: Int,
                                val tier: MatchingTier): FingerprintResponse
