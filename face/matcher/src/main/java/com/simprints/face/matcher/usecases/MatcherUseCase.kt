package com.simprints.face.matcher.usecases

import com.simprints.face.matcher.MatchParams
import com.simprints.face.matcher.MatchResultItem

interface MatcherUseCase {

    val crashReportTag: String
    val matcherName: String

    suspend operator fun invoke(
        matchParams: MatchParams,
        onLoadingCandidates: (tag: String) -> Unit = {},
        onMatching: (tag: String) -> Unit = {},
    ): List<MatchResultItem>

}
