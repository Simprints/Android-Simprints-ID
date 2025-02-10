package com.simprints.matcher.usecases

import com.simprints.infra.logging.LoggingConstants
import com.simprints.matcher.MatchParams
import com.simprints.matcher.MatchResultItem

internal interface MatcherUseCase {
    val crashReportTag: LoggingConstants.CrashReportTag

    /**
     * Returns a MatcherResult which contains a list of [MatchResultItem]s sorted by confidence score in descending order,
     * the total number of candidates that were considered and the name of the matcher that was used
     */
    suspend operator fun invoke(
        matchParams: MatchParams,
        onLoadingStarted: (totalCandidates: Int) -> Unit = {},
        onCandidateLoaded: () -> Unit = {},
    ): MatcherResult

    data class MatcherResult(
        val matchResultItems: List<MatchResultItem>,
        val totalCandidates: Int,
        val matcherName: String,
    )
}
