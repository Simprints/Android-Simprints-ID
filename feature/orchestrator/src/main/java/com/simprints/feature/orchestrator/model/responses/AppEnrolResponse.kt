package com.simprints.feature.orchestrator.model.responses

import com.simprints.infra.orchestration.moduleapi.app.responses.IAppEnrolResponse
import com.simprints.infra.orchestration.moduleapi.app.responses.IAppResponseType
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class AppEnrolResponse(
    override val guid: String,
    override val type: IAppResponseType = IAppResponseType.ENROL
) : IAppEnrolResponse
