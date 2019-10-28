package com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests

import com.simprints.fingerprint.data.domain.fingerprint.Fingerprint
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class FingerprintMatchRequest(
    val probeFingerprintSamples: List<Fingerprint>,
    val queryForCandidates: Serializable
) : FingerprintRequest
