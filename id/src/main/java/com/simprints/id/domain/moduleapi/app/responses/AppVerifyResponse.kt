package com.simprints.id.domain.moduleapi.app.responses

import com.simprints.id.domain.moduleapi.app.responses.entities.MatchResult
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppVerifyResponse(val matchingResult: MatchResult): AppResponse {

    @IgnoredOnParcel override val type: AppResponseType = AppResponseType.VERIFY
}
