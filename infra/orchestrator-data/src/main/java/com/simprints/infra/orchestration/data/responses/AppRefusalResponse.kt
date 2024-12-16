package com.simprints.infra.orchestration.data.responses

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.feature.exitform.ExitFormResult
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
@ExcludedFromGeneratedTestCoverageReports("Data struct")
data class AppRefusalResponse(
    val reason: String,
    val extra: String,
) : AppResponse() {
    companion object {
        fun fromResult(result: ExitFormResult) = AppRefusalResponse(
            result
                .submittedOption()
                ?.answer
                ?.name
                .orEmpty(),
            result.reason.orEmpty(),
        )
    }
}
