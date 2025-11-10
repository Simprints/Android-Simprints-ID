package com.simprints.matcher.screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.DecisionPolicy
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.matching.FaceMatchResult
import com.simprints.infra.matching.FingerprintMatchResult
import com.simprints.infra.matching.MatchParams
import com.simprints.infra.matching.MatchResultItem
import com.simprints.infra.matching.usecase.FaceMatcherUseCase
import com.simprints.infra.matching.usecase.FingerprintMatcherUseCase
import com.simprints.infra.matching.usecase.MatcherUseCase
import com.simprints.infra.matching.usecase.SaveMatchEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.Serializable
import javax.inject.Inject

@HiltViewModel
internal class MatchViewModel @Inject constructor(
    private val faceMatcher: FaceMatcherUseCase,
    private val fingerprintMatcher: FingerprintMatcherUseCase,
    private val saveMatchEvent: SaveMatchEventUseCase,
    private val authStore: AuthStore,
    private val configManager: ConfigManager,
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

    val matchResponse: LiveData<LiveDataEventWithContent<Serializable>>
        get() = _matchResponse
    private val _matchResponse = MutableLiveData<LiveDataEventWithContent<Serializable>>()

    fun setupMatch(params: MatchParams) = viewModelScope.launch {
        if (isMatcherRunning) return@launch
        isMatcherRunning = true
        isInitialized = true
        val startTime = timeHelper.now()

        val isFaceMatch = params.isFaceMatch()
        val matcherUseCase = when {
            isFaceMatch -> faceMatcher
            else -> fingerprintMatcher
        }
        val project = configManager.getProject(authStore.signedInProjectId)
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
                        matcherState.matchResultItems,
                        matcherState.matchBatches,
                    )

                    setMatchState(matcherState.totalCandidates, matcherState.matchResultItems, decisionPolicy)

                    // wait a bit for the user to see the results
                    delay(MATCHING_END_WAIT_TIME_MS)

                    _matchResponse.send(
                        when {
                            isFaceMatch -> FaceMatchResult(matcherState.matchResultItems, params.faceSDK!!)
                            else -> FingerprintMatchResult(matcherState.matchResultItems, params.fingerprintSDK!!)
                        },
                    )
                }
            }
        }
    }

    private suspend fun getDecisionPolicy(params: MatchParams): DecisionPolicy {
        val config = configManager.getProjectConfiguration()
        val faceSDK = params.faceSDK
        val fingerprintSDK = params.fingerprintSDK
        val policy = when {
            faceSDK != null -> config.face?.getSdkConfiguration(faceSDK)?.decisionPolicy
            fingerprintSDK != null -> config.fingerprint?.getSdkConfiguration(fingerprintSDK)?.decisionPolicy
            else -> null
        }
        return policy ?: fallbackDecisionPolicy()
    }

    private fun setMatchState(
        candidatesMatched: Int,
        results: List<MatchResultItem>,
        decisionPolicy: DecisionPolicy,
    ) {
        val veryGoodMatches = results.count { decisionPolicy.high <= it.confidence }
        val goodMatches =
            results.count { decisionPolicy.medium <= it.confidence && it.confidence < decisionPolicy.high }
        val fairMatches =
            results.count { decisionPolicy.low <= it.confidence && it.confidence < decisionPolicy.medium }

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
