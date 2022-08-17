package com.simprints.face.match

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.infra.logging.LoggingConstants.CrashReportTag
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.face.controllers.core.events.FaceSessionEventsManager
import com.simprints.face.controllers.core.events.model.MatchEntry
import com.simprints.face.controllers.core.events.model.Matcher
import com.simprints.face.controllers.core.events.model.OneToManyMatchEvent
import com.simprints.face.controllers.core.events.model.OneToOneMatchEvent
import com.simprints.face.controllers.core.flow.Action
import com.simprints.face.controllers.core.flow.MasterFlowManager
import com.simprints.face.controllers.core.repository.FaceDbManager
import com.simprints.face.controllers.core.timehelper.FaceTimeHelper
import com.simprints.face.data.db.person.FaceIdentity
import com.simprints.face.data.db.person.FaceSample
import com.simprints.face.data.moduleapi.face.requests.FaceMatchRequest
import com.simprints.face.data.moduleapi.face.responses.FaceMatchResponse
import com.simprints.face.data.moduleapi.face.responses.entities.FaceMatchResult
import com.simprints.face.match.rankone.RankOneFaceMatcher
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import java.io.Serializable

class FaceMatchViewModel(
    private val masterFlowManager: MasterFlowManager,
    private val faceDbManager: FaceDbManager,
    private val faceMatcher: FaceMatcher,
    private val faceSessionEventsManager: FaceSessionEventsManager,
    private val faceTimeHelper: FaceTimeHelper) : ViewModel() {
    companion object {
        const val returnCount = 10
        const val veryGoodMatchThreshold = 50.0
        const val goodMatchThreshold = 35.0
        const val fairMatchThreshold = 20.0
        const val matchingEndWaitTimeInMillis = 1000L
    }

    val matchState: MutableLiveData<MatchState> = MutableLiveData(MatchState.NotStarted)
    val faceMatchResponse: MutableLiveData<LiveDataEventWithContent<FaceMatchResponse>> =
        MutableLiveData()

    fun setupMatch(faceRequest: FaceMatchRequest) = viewModelScope.launch {
        val startTime = faceTimeHelper.now()

        val candidates = loadCandidates(faceRequest.queryForCandidates)
        val results = matchCandidates(faceRequest.probeFaceSamples, candidates)
        val sortedResults = getSortedResult(results)
        val maxFilteredResults = getMaxFilteredResults(sortedResults)

        val endTime = faceTimeHelper.now()

        sendMatchEvent(
            startTime,
            endTime,
            masterFlowManager.getCurrentAction(),
            faceRequest.queryForCandidates,
            sortedResults.size,
            maxFilteredResults
        )

        sendFaceMatchResponse(sortedResults.size, maxFilteredResults)
    }

    private suspend fun loadCandidates(queryForCandidates: Serializable): Flow<FaceIdentity> {
        Simber.tag(CrashReportTag.FACE_MATCHING.name).i("Loading candidates")
        matchState.value = MatchState.LoadingCandidates
        return faceDbManager.loadPeople(queryForCandidates)
    }

    private suspend fun matchCandidates(
        probeFaceSamples: List<FaceSample>,
        candidates: Flow<FaceIdentity>
    ): Flow<FaceMatchResult> {
        Simber.tag(CrashReportTag.FACE_MATCHING.name).i("Matching probe against candidates")
        matchState.postValue(MatchState.Matching)
        return getConcurrentMatchResultsForCandidates(probeFaceSamples, candidates)
    }


    private suspend fun getConcurrentMatchResultsForCandidates(
        probeFaceSamples: List<FaceSample>,
        candidates: Flow<FaceIdentity>
    ) = candidates.map() { candidate ->
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
    }

    private fun sendMatchEvent(
        startTime: Long,
        endTime: Long,
        action: Action,
        queryForCandidates: Serializable,
        candidatesCount: Int,
        results: List<FaceMatchResult>
    ) {
        val matchEntries = results.map { MatchEntry(it.guid, it.confidence) }
        val event = if (action == Action.VERIFY)
            getOneToOneEvent(startTime, endTime, queryForCandidates, matchEntries.first())
        else
            getOneToManyEvent(startTime, endTime, queryForCandidates, candidatesCount, matchEntries)

        faceSessionEventsManager.addEventInBackground(event)
    }

    private fun getOneToManyEvent(
        startTime: Long,
        endTime: Long,
        queryForCandidates: Serializable,
        candidatesCount: Int,
        matchEntries: List<MatchEntry>
    ): OneToManyMatchEvent =
        OneToManyMatchEvent(
            startTime,
            endTime,
            queryForCandidates,
            candidatesCount,
            faceMatcher.getEventMatcher(),
            matchEntries
        )

    private fun getOneToOneEvent(
        startTime: Long,
        endTime: Long,
        queryForCandidates: Serializable,
        matchEntry: MatchEntry
    ): OneToOneMatchEvent =
        OneToOneMatchEvent(
            startTime,
            endTime,
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
