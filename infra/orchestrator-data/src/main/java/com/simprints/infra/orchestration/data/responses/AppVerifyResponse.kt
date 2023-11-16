package com.simprints.infra.orchestration.data.responses

import kotlinx.parcelize.Parcelize

@Parcelize
data class AppVerifyResponse(
    val matchResult: AppMatchResult,
) : AppResponse()


