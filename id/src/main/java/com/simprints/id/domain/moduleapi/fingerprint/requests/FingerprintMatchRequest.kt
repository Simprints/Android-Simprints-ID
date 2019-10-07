package com.simprints.id.domain.moduleapi.fingerprint.requests

import com.simprints.id.data.db.person.domain.FingerprintSample
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class FingerprintMatchRequest(
    val probeFingerprintSamples: List<FingerprintSample>,
    val queryForCandidates: Serializable
) : FingerprintRequest

