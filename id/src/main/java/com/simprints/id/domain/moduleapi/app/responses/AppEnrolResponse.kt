package com.simprints.id.domain.moduleapi.app.responses

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppEnrolResponse(val guid: String): AppResponse {

    @IgnoredOnParcel override val type: AppResponseType = AppResponseType.ENROL
}
