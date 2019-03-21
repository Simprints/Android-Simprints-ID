package com.simprints.id.domain.moduleapi.fingerprint.responses

import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintRefusalFormAnswer
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintRefusalFormResponse(val answer: FingerprintRefusalFormAnswer): AppResponse
