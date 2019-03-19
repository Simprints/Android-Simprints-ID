package com.simprints.fingerprint.data.domain.responses

import com.simprints.fingerprint.data.domain.matching.result.MatchingResult
import kotlinx.android.parcel.Parcelize

@Parcelize
class FingerprintIdentifyResponse(val identifications: List<MatchingResult>) : FingerprintResponse
