package com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses

import kotlinx.android.parcel.Parcelize

@Parcelize
class FingerprintRefusalFormResponse(val reason: String,
                                     val extra: String) : FingerprintResponse
