package com.simprints.id.domain.moduleapi.app.responses

import com.simprints.id.domain.moduleapi.app.responses.entities.MatchResult
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AppVerifyResponse(val matchingResult: MatchResult): AppResponse
