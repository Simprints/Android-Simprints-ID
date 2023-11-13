package com.simprints.feature.orchestrator.model.responses

import com.simprints.feature.exitform.ExitFormResult
import com.simprints.infra.orchestration.moduleapi.app.responses.IAppRefusalFormResponse
import com.simprints.infra.orchestration.moduleapi.app.responses.IAppResponseType
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class AppRefusalResponse(
    override val reason: String,
    override val extra: String,
    override val type: IAppResponseType = IAppResponseType.REFUSAL
) : IAppRefusalFormResponse {

    companion object {

        fun fromResult(result: ExitFormResult) = AppRefusalResponse(
            result.submittedOption()?.answer?.name.orEmpty(),
            result.reason.orEmpty(),
        )
    }
}
