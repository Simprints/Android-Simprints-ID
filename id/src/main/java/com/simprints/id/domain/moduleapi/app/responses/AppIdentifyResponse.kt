package com.simprints.id.domain.moduleapi.app.responses

import com.simprints.id.domain.moduleapi.app.responses.entities.MatchResult
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AppIdentifyResponse(val identifications: List<MatchResult>,
                               val sessionId: String): AppResponse {
    @IgnoredOnParcel override val type: AppResponseType = AppResponseType.IDENTIFY
}
