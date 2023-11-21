package com.simprints.infra.orchestration.data.responses

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import kotlinx.parcelize.Parcelize

@Parcelize
@ExcludedFromGeneratedTestCoverageReports("Data struct")
data class AppIdentifyResponse(
    val identifications: List<AppMatchResult>,
    val sessionId: String,
) : AppResponse()
