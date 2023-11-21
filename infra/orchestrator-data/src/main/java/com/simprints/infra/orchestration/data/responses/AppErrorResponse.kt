package com.simprints.infra.orchestration.data.responses

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.response.AppErrorReason
import kotlinx.parcelize.Parcelize

@Parcelize
@ExcludedFromGeneratedTestCoverageReports("Data struct")
data class AppErrorResponse(
    val reason: AppErrorReason,
) : AppResponse()
