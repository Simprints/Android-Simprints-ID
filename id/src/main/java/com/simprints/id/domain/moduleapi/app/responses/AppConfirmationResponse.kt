package com.simprints.id.domain.moduleapi.app.responses

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppConfirmationResponse(val identificationOutcome: Boolean) : AppResponse {

    @IgnoredOnParcel
    override val type: AppResponseType = AppResponseType.CONFIRMATION

}
