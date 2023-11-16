package com.simprints.infra.orchestration.data.responses

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import kotlinx.parcelize.Parcelize

@Parcelize
@ExcludedFromGeneratedTestCoverageReports("Data struct")
data class AppEnrolResponse(
    val guid: String,
) : AppResponse()
