package com.simprints.id.domain.responses

import com.simprints.fingerprint.data.domain.refusal.RefusalFormAnswer
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RefusalFormResponse(val answer: com.simprints.fingerprint.data.domain.refusal.RefusalFormAnswer): Response
