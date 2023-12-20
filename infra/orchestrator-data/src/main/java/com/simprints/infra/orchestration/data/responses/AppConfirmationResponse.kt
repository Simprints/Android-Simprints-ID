package com.simprints.infra.orchestration.data.responses

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
@ExcludedFromGeneratedTestCoverageReports("Data struct")
data class AppConfirmationResponse(
    val identificationOutcome: Boolean,
) : AppResponse()
