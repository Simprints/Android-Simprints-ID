package com.simprints.id.domain.responses

import com.simprints.id.domain.matching.IdentificationResult
import kotlinx.android.parcel.Parcelize

@Parcelize
data class IdentifyResponse(val identifications: List<IdentificationResult>,
                            val sessionId: String): Response
