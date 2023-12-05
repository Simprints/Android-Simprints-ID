package com.simprints.matcher.usecases

import com.simprints.matcher.MatchParams
import com.simprints.matcher.MatchResultItem

internal interface MatcherUseCase {

    val crashReportTag: String
    val matcherName: String

    /**
     * Returns a list of [MatchResultItem]s sorted by confidence score in descending order
     * and the total number of candidates that were considered.
     */
    suspend operator fun invoke(
        matchParams: MatchParams,
        onLoadingCandidates: (tag: String) -> Unit = {},
    ): Pair<List<MatchResultItem>, Int>

}
