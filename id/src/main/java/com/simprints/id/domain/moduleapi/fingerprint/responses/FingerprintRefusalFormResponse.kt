package com.simprints.id.domain.moduleapi.fingerprint.responses

import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintRefusalFormReason
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintRefusalFormResponse(val reason: FingerprintRefusalFormReason?,
                                          val optionalText: String = ""): FingerprintResponse
