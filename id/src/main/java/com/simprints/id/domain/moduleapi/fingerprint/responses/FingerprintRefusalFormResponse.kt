package com.simprints.id.domain.moduleapi.fingerprint.responses

import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintRefusalFormAnswer
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintRefusalFormResponse(val answer: FingerprintRefusalFormAnswer): FingerprintResponse
