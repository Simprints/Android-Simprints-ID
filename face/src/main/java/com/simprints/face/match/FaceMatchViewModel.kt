package com.simprints.face.match

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.coroutines.DefaultDispatcherProvider
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.core.tools.extentions.concurrentMap
import com.simprints.face.controllers.core.crashreport.FaceCrashReportManager
import com.simprints.face.controllers.core.crashreport.FaceCrashReportTag
import com.simprints.face.controllers.core.crashreport.FaceCrashReportTrigger
import com.simprints.face.controllers.core.events.FaceSessionEventsManager
import com.simprints.face.controllers.core.events.model.MatchEntry
import com.simprints.face.controllers.core.events.model.Matcher
import com.simprints.face.controllers.core.events.model.OneToManyMatchEvent
import com.simprints.face.controllers.core.events.model.OneToOneMatchEvent
import com.simprints.face.controllers.core.preferencesManager.FacePreferencesManager
import com.simprints.face.controllers.core.repository.FaceDbManager
import com.simprints.face.controllers.core.timehelper.FaceTimeHelper
import com.simprints.face.data.db.person.FaceIdentity
import com.simprints.face.data.db.person.FaceSample
import com.simprints.face.data.moduleapi.face.requests.FaceMatchRequest
import com.simprints.face.data.moduleapi.face.responses.FaceMatchResponse
import com.simprints.face.data.moduleapi.face.responses.entities.FaceMatchResult
import com.simprints.face.match.rankone.RankOneFaceMatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import java.io.Serializable

class FaceMatchViewModel(
    private val faceDbManager: FaceDbManager,
    private val faceMatcher: FaceMatcher,
    private val preferencesManager: FacePreferencesManager,
    private val crashReportManager: FaceCrashReportManager,
    private val faceSessionEventsManager: FaceSessionEventsManager,
    private val faceTimeHelper: FaceTimeHelper,
    private val dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider()
) : ViewModel() {
    companion object {
        const val returnCount = 10
        const val veryGoodMatchThreshold = 50.0
        const val goodMatchThreshold = 35.0
        const val fairMatchThreshold = 20.0
        const val matchingEndWaitTimeInMillis = 1000L
    }

    private val startTime = faceTimeHelper.now()

    val matchState: MutableLiveData<MatchState> = MutableLiveData(MatchState.NotStarted)
    val faceMatchResponse: MutableLiveData<LiveDataEventWithContent<FaceMatchResponse>> =
        MutableLiveData()

    fun setupMatch(faceRequest: FaceMatchRequest) = viewModelScope.launch {
        val candidates = loadCandidates(faceRequest.queryForCandidates)
        val results = matchCandidates(faceRequest.probeFaceSamples, candidates)
        val sortedResults = getSortedResult(results)
        val maxFilteredResults = getMaxFilteredResults(sortedResults)

        sendMatchEvent(
            faceRequest.queryForCandidates,
            sortedResults.size,
            maxFilteredResults.map { MatchEntry(it.guid, it.confidence) })

        sendFaceMatchResponse(sortedResults.size, maxFilteredResults)
    }

    private suspend fun loadCandidates(queryForCandidates: Serializable): Flow<FaceIdentity> {
        crashReportManager.logMessageForCrashReport(
            FaceCrashReportTag.FACE_MATCHING,
            FaceCrashReportTrigger.UI,
            message = "Loading candidates"
        )
        matchState.value = MatchState.LoadingCandidates
        return faceDbManager.loadPeople(queryForCandidates)
    }

    private suspend fun matchCandidates(
        probeFaceSamples: List<FaceSample>,
        candidates: Flow<FaceIdentity>
    ): Flow<FaceMatchResult> {
        crashReportManager.logMessageForCrashReport(
            FaceCrashReportTag.FACE_MATCHING,
            FaceCrashReportTrigger.UI,
            message = "Matching probe against candidates"
        )
        matchState.postValue(MatchState.Matching)
        return getConcurrentMatchResultsForCandidates(probeFaceSamples, candidates)
    }

    /**
     * Run in a concurrent map, making the processing much faster
     */
    private suspend fun getConcurrentMatchResultsForCandidates(
        probeFaceSamples: List<FaceSample>,
        candidates: Flow<FaceIdentity>
    ) = candidates.concurrentMap(dispatcherProvider.default()) { candidate ->
        FaceMatchResult(
            candidate.faceId,
            faceMatcher.getHighestComparisonScoreForCandidate(probeFaceSamples, candidate)
        )
    }

    private suspend fun getSortedResult(results: Flow<FaceMatchResult>): List<FaceMatchResult> =
        results.toList().sortedByDescending { it.confidence }

    private fun sendFaceMatchResponse(candidatesMatched: Int, results: List<FaceMatchResult>) {
        val veryGoodMatches = results.count { veryGoodMatchThreshold <= it.confidence }
        val goodMatches =
            results.count { goodMatchThreshold <= it.confidence && it.confidence < veryGoodMatchThreshold }
        val fairMatches =
            results.count { fairMatchThreshold <= it.confidence && it.confidence < goodMatchThreshold }

        matchState.value = MatchState.Finished(
            candidatesMatched,
            results.size,
            veryGoodMatches,
            goodMatches,
            fairMatches
        )
        val response = FaceMatchResponse(results)

        faceMatchResponse.send(response)
    }

    private fun getMaxFilteredResults(sortedResults: List<FaceMatchResult>): List<FaceMatchResult> {
        return sortedResults
            .take(returnCount)
            .filter { it.confidence >= preferencesManager.faceMatchThreshold }
    }

    private fun sendMatchEvent(queryForCandidates: Serializable, candidatesCount: Int, matchEntries: List<MatchEntry>) {
        val event = if (candidatesCount > 1)
            getOneToManyEvent(queryForCandidates, candidatesCount, matchEntries)
        else
            getOneToOneEvent(queryForCandidates, matchEntries.first())

        faceSessionEventsManager.addEventInBackground(event)
    }

    private fun getOneToManyEvent(
        queryForCandidates: Serializable,
        candidatesCount: Int,
        matchEntries: List<MatchEntry>
    ): OneToManyMatchEvent =
        OneToManyMatchEvent(
            startTime,
            faceTimeHelper.now(),
            queryForCandidates,
            candidatesCount,
            faceMatcher.getEventMatcher(),
            matchEntries
        )

    private fun getOneToOneEvent(queryForCandidates: Serializable, matchEntry: MatchEntry): OneToOneMatchEvent =
        OneToOneMatchEvent(
            startTime,
            faceTimeHelper.now(),
            queryForCandidates,
            faceMatcher.getEventMatcher(),
            matchEntry
        )

    private fun FaceMatcher.getEventMatcher(): Matcher =
        if (this is RankOneFaceMatcher) Matcher.RANK_ONE else Matcher.UNKNOWN

    sealed class MatchState {
        object NotStarted : MatchState()
        object Error : MatchState()
        object LoadingCandidates : MatchState()
        object Matching : MatchState()
        data class Finished(
            val candidatesMatched: Int,
            val returnSize: Int,
            val veryGoodMatches: Int,
            val goodMatches: Int,
            val fairMatches: Int
        ) : MatchState()
    }
}
