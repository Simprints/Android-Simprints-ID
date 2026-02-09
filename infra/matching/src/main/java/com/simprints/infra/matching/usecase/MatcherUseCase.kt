package com.simprints.infra.matching.usecase

import com.simprints.core.domain.comparison.ComparisonResult
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.matching.MatchBatchInfo
import com.simprints.infra.matching.MatchParams
import kotlinx.coroutines.flow.Flow

interface MatcherUseCase {
    val crashReportTag: LoggingConstants.CrashReportTag

    operator fun invoke(
        matchParams: MatchParams,
        project: Project,
    ): Flow<MatcherState>

    sealed class MatcherState {
        data class LoadingStarted(
            val totalCandidates: Int,
        ) : MatcherState()

        data object CandidateLoaded : MatcherState()

        data class Success(
            val comparisonResults: List<ComparisonResult>,
            val matchBatches: List<MatchBatchInfo>,
            val totalCandidates: Int,
            val matcherName: String,
        ) : MatcherState()
    }
}
