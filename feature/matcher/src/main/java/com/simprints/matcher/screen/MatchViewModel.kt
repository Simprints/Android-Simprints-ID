package com.simprints.matcher.screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.comparison.ComparisonResult
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.DecisionPolicy
import com.simprints.infra.config.store.models.getModalitySdkConfig
import com.simprints.infra.matching.MatchParams
import com.simprints.infra.matching.MatchResult
import com.simprints.infra.matching.usecase.FaceMatcherUseCase
import com.simprints.infra.matching.usecase.FingerprintMatcherUseCase
import com.simprints.infra.matching.usecase.MatcherUseCase
import com.simprints.infra.matching.usecase.SaveMatchEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class MatchViewModel @Inject constructor(
    private val faceMatcher: FaceMatcherUseCase,
    private val fingerprintMatcher: FingerprintMatcherUseCase,
    private val saveMatchEvent: SaveMatchEventUseCase,
    private val configRepository: ConfigRepository,
    private val timeHelper: TimeHelper,
) : ViewModel() {
    var isMatcherRunning = false
        private set
    var isInitialized = false
        private set
    private var candidatesLoaded = 0

    var shouldCheckPermission: Boolean = true

    val matchState: LiveData<MatchState>
        get() = _matchState
    private val _matchState = MutableLiveData<MatchState>(MatchState.NotStarted)

    val matchResponse: LiveData<LiveDataEventWithContent<MatchResult>>
        get() = _matchResponse
    private val _matchResponse = MutableLiveData<LiveDataEventWithContent<MatchResult>>()

    fun setupMatch(params: MatchParams) = viewModelScope.launch {
        if (isMatcherRunning) return@launch
        isMatcherRunning = true
        isInitialized = true
        val startTime = timeHelper.now()

        val matcherUseCase = when (params.bioSdk.modality()) {
            Modality.FACE -> faceMatcher
            Modality.FINGERPRINT -> fingerprintMatcher
        }
        val project = configRepository.getProject() ?: return@launch
        val decisionPolicy = getDecisionPolicy(params)

        candidatesLoaded = 0
        matcherUseCase(params, project).collect { matcherState ->
            when (matcherState) {
                MatcherUseCase.MatcherState.CandidateLoaded -> {
                    (_matchState.value as? MatchState.LoadingCandidates)?.let { currentState ->
                        candidatesLoaded++
                        _matchState.postValue(currentState.copy(loaded = candidatesLoaded))
                    }
                }

                is MatcherUseCase.MatcherState.LoadingStarted -> {
                    _matchState.postValue(MatchState.LoadingCandidates(matcherState.totalCandidates, loaded = 0))
                }

                is MatcherUseCase.MatcherState.Success -> {
                    val endTime = timeHelper.now()

                    saveMatchEvent(
                        startTime,
                        endTime,
                        params,
                        matcherState.totalCandidates,
                        matcherState.matcherName,
                        matcherState.comparisonResults,
                        matcherState.matchBatches,
                    )

                    setMatchState(matcherState.totalCandidates, matcherState.comparisonResults, decisionPolicy)

                    // wait a bit for the user to see the results
                    delay(MATCHING_END_WAIT_TIME_MS)

                    _matchResponse.send(MatchResult(matcherState.comparisonResults, params.bioSdk))
                }
            }
        }
    }

    private suspend fun getDecisionPolicy(params: MatchParams): DecisionPolicy = configRepository
        .getProjectConfiguration()
        .getModalitySdkConfig(params.bioSdk)
        ?.decisionPolicy
        ?: fallbackDecisionPolicy()

    private fun setMatchState(
        candidatesMatched: Int,
        results: List<ComparisonResult>,
        decisionPolicy: DecisionPolicy,
    ) {
        val veryGoodMatches = results.count { decisionPolicy.high <= it.comparisonScore }
        val goodMatches =
            results.count { decisionPolicy.medium <= it.comparisonScore && it.comparisonScore < decisionPolicy.high }
        val fairMatches =
            results.count { decisionPolicy.low <= it.comparisonScore && it.comparisonScore < decisionPolicy.medium }

        _matchState.postValue(
            MatchState.Finished(
                candidatesMatched,
                results.size,
                veryGoodMatches,
                goodMatches,
                fairMatches,
            ),
        )
    }

    fun noPermission(neverAskAgain: Boolean) {
        _matchState.postValue(MatchState.NoPermission(shouldOpenSettings = neverAskAgain))
    }

    sealed class MatchState {
        data object NotStarted : MatchState()

        data class LoadingCandidates(
            val total: Int,
            val loaded: Int,
        ) : MatchState()

        data object Matching : MatchState()

        data class NoPermission(
            val shouldOpenSettings: Boolean,
        ) : MatchState()

        data class Finished(
            val candidatesMatched: Int,
            val returnSize: Int,
            val veryGoodMatches: Int,
            val goodMatches: Int,
            val fairMatches: Int,
        ) : MatchState()
    }

    // TODO This configuration should be provided by SDK or project configuration
    //   https://simprints.atlassian.net/browse/CORE-2923

    // Old hardcoded values that could be used to if there is no SDK configuration for the given SDK
    private fun fallbackDecisionPolicy() = DecisionPolicy(
        low = FAIR_MATCH_THRESHOLD,
        medium = GOOD_MATCH_THRESHOLD,
        high = VERY_GOOD_MATCH_THRESHOLD,
    )

    companion object {
        private const val VERY_GOOD_MATCH_THRESHOLD = 50
        private const val GOOD_MATCH_THRESHOLD = 35
        private const val FAIR_MATCH_THRESHOLD = 20
        private const val MATCHING_END_WAIT_TIME_MS = 1000L
    }
}
