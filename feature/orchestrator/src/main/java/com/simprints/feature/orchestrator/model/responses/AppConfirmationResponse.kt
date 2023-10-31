package com.simprints.feature.orchestrator.model.responses

import com.simprints.moduleapi.app.responses.IAppConfirmationResponse
import com.simprints.moduleapi.app.responses.IAppResponseType
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppConfirmationResponse(
    override val identificationOutcome: Boolean,
    override val type: IAppResponseType = IAppResponseType.CONFIRMATION
) : IAppConfirmationResponse
