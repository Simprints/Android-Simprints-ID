package com.simprints.infra.matching.usecase

import com.simprints.core.domain.sample.MatchConfidence
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.matching.MatchBatchInfo
import com.simprints.infra.matching.MatchParams
import kotlinx.coroutines.flow.Flow

interface MatcherUseCase {
    val crashReportTag: LoggingConstants.CrashReportTag

    /**
     * Returns a MatcherResult which contains a list of [MatchConfidence]s sorted by confidence score in descending order,
     * the total number of candidates that were considered and the name of the matcher that was used
     */
    suspend operator fun invoke(
        matchParams: MatchParams,
        project: Project,
    ): Flow<MatcherState>

    sealed class MatcherState {
        data class LoadingStarted(
            val totalCandidates: Int,
        ) : MatcherState()

        data object CandidateLoaded : MatcherState()

        data class Success(
            val matchResultItems: List<MatchConfidence>,
            val matchBatches: List<MatchBatchInfo>,
            val totalCandidates: Int,
            val matcherName: String,
        ) : MatcherState()
    }
}
