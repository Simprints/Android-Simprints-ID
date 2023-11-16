package com.simprints.infra.orchestration.data.responses

import kotlinx.parcelize.Parcelize

@Parcelize
data class AppIdentifyResponse(
    val identifications: List<AppMatchResult>,
    val sessionId: String,
) : AppResponse()
