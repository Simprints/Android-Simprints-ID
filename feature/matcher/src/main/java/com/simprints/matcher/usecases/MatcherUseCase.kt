package com.simprints.matcher.usecases

import com.simprints.matcher.MatchParams
import com.simprints.matcher.MatchResultItem

internal interface MatcherUseCase {

    val crashReportTag: String
    val matcherName: String

    suspend operator fun invoke(
        matchParams: MatchParams,
        onLoadingCandidates: (tag: String) -> Unit = {},
        onMatching: (tag: String) -> Unit = {},
    ): List<MatchResultItem>

}
