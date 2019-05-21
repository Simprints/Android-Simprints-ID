package com.simprints.id.domain.moduleapi.app.responses

import com.simprints.id.domain.moduleapi.app.responses.entities.RefusalFormAnswer
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AppRefusalFormResponse(val answer: RefusalFormAnswer): AppResponse {

    @IgnoredOnParcel override val type: AppResponseType = AppResponseType.REFUSAL
}
