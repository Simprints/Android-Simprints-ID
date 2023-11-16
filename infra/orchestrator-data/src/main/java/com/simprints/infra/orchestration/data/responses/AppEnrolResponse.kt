package com.simprints.infra.orchestration.data.responses

import kotlinx.parcelize.Parcelize

@Parcelize
data class AppEnrolResponse(
    val guid: String,
) : AppResponse()
