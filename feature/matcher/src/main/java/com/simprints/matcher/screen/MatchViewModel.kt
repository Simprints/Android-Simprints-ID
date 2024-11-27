package com.simprints.matcher.screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.logging.Simber
import com.simprints.matcher.FaceMatchResult
import com.simprints.matcher.FingerprintMatchResult
import com.simprints.matcher.MatchParams
import com.simprints.matcher.MatchResultItem
import com.simprints.matcher.usecases.FaceMatcherUseCase
import com.simprints.matcher.usecases.FingerprintMatcherUseCase
import com.simprints.matcher.usecases.SaveMatchEventUseCase
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
    private val timeHelper: TimeHelper,
) : ViewModel() {

    var isInitialized = false
        private set

    var shouldCheckPermission: Boolean = true

    val matchState: LiveData<MatchState>
        get() = _matchState
    private val _matchState = MutableLiveData<MatchState>(MatchState.NotStarted)

    val matchResponse: LiveData<LiveDataEventWithContent<Serializable>>
        get() = _matchResponse
    private val _matchResponse = MutableLiveData<LiveDataEventWithContent<Serializable>>()

    fun setupMatch(params: MatchParams) = viewModelScope.launch {
        isInitialized = true
        val startTime = timeHelper.now()

        val isFaceMatch = params.isFaceMatch()
        val matcherUseCase = when {
            isFaceMatch -> faceMatcher
            else -> fingerprintMatcher
        }

        val matcherResult = matcherUseCase(
            params,
            onLoadingCandidates = { tag ->
                Simber.tag(tag).i("Loading candidates")
                _matchState.postValue(MatchState.LoadingCandidates)
            },
        )

        val endTime = timeHelper.now()

        saveMatchEvent(
            startTime,
            endTime,
            params,
            matcherResult.totalCandidates,
            matcherResult.matcherName,
            matcherResult.matchResultItems
        )

        setMatchState(matcherResult.totalCandidates, matcherResult.matchResultItems)

        // wait a bit for the user to see the results
        delay(MATCHING_END_WAIT_TIME_MS)

        _matchResponse.send(
            when {
                isFaceMatch -> FaceMatchResult(matcherResult.matchResultItems)
                else -> FingerprintMatchResult(matcherResult.matchResultItems, params.fingerprintSDK!!)
            }
        )
    }

    private fun setMatchState(candidatesMatched: Int, results: List<MatchResultItem>) {
        val veryGoodMatches = results.count { VERY_GOOD_MATCH_THRESHOLD <= it.confidence }
        val goodMatches =
            results.count { GOOD_MATCH_THRESHOLD <= it.confidence && it.confidence < VERY_GOOD_MATCH_THRESHOLD }
        val fairMatches =
            results.count { FAIR_MATCH_THRESHOLD <= it.confidence && it.confidence < GOOD_MATCH_THRESHOLD }

        _matchState.postValue(
            MatchState.Finished(
                candidatesMatched,
                results.size,
                veryGoodMatches,
                goodMatches,
                fairMatches
            )
        )
    }

    fun noPermission(neverAskAgain: Boolean) {
        _matchState.postValue(MatchState.NoPermission(shouldOpenSettings = neverAskAgain))
    }

    sealed class MatchState {
        data object NotStarted : MatchState()
        data object LoadingCandidates : MatchState()
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
    companion object {

        private const val VERY_GOOD_MATCH_THRESHOLD = 50.0
        private const val GOOD_MATCH_THRESHOLD = 35.0
        private const val FAIR_MATCH_THRESHOLD = 20.0
        private const val MATCHING_END_WAIT_TIME_MS = 1000L
    }
}
