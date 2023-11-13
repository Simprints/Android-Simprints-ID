package com.simprints.feature.orchestrator.model.responses

import com.simprints.infra.orchestration.moduleapi.app.responses.IAppConfirmationResponse
import com.simprints.infra.orchestration.moduleapi.app.responses.IAppResponseType
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class AppConfirmationResponse(
    override val identificationOutcome: Boolean,
    override val type: IAppResponseType = IAppResponseType.CONFIRMATION
) : IAppConfirmationResponse
