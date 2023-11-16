package com.simprints.infra.orchestration.data.responses

import kotlinx.parcelize.Parcelize

@Parcelize
data class AppConfirmationResponse(
    val identificationOutcome: Boolean,
) : AppResponse()
