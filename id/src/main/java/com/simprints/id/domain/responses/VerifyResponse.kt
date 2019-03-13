package com.simprints.id.domain.responses

import com.simprints.id.domain.matching.Tier
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VerifyResponse(val guid: String,
                          val confidence: Int,
                          val tier: Tier): Response
