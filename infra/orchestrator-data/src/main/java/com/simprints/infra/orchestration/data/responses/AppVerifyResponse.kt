package com.simprints.infra.orchestration.data.responses

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import kotlinx.parcelize.Parcelize

@Parcelize
@ExcludedFromGeneratedTestCoverageReports("Data struct")
data class AppVerifyResponse(
    val matchResult: AppMatchResult,
) : AppResponse()


