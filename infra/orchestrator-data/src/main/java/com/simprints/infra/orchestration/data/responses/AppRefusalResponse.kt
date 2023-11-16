package com.simprints.infra.orchestration.data.responses

import com.simprints.feature.exitform.ExitFormResult
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppRefusalResponse(
    val reason: String,
    val extra: String,
) : AppResponse() {

    companion object {

        fun fromResult(result: ExitFormResult) = AppRefusalResponse(
            result.submittedOption()?.answer?.name.orEmpty(),
            result.reason.orEmpty(),
        )
    }
}
