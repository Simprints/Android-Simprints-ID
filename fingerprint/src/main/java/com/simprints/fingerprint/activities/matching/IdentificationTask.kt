package com.simprints.fingerprint.activities.matching

import android.content.Intent
import android.util.Log
import com.simprints.fingerprint.activities.matching.request.MatchingTaskIdentifyRequest
import com.simprints.fingerprint.activities.matching.request.MatchingTaskRequest
import com.simprints.fingerprint.activities.matching.result.MatchingTaskIdentifyResult
import com.simprints.fingerprint.activities.matching.result.MatchingTaskResult
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTag.MATCHING
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTrigger.UI
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.MatchEntry
import com.simprints.fingerprint.controllers.core.eventData.model.OneToManyMatchEvent
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.controllers.core.preferencesManager.MatchPoolType
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.data.domain.matching.MatchingResult
import com.simprints.fingerprint.data.domain.matching.MatchingTier
import com.simprints.fingerprint.data.domain.person.Person
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import com.simprints.fingerprintmatcher.LibMatcher
import io.reactivex.Completable
import io.reactivex.Single
import java.util.*

class IdentificationTask(private val viewModel: MatchingViewModel,
                         matchingRequest: MatchingTaskRequest,
                         private val dbManager: FingerprintDbManager,
                         private val sessionEventsManager: FingerprintSessionEventsManager,
                         private val crashReportManager: FingerprintCrashReportManager,
                         private val timeHelper: FingerprintTimeHelper,
                         private val preferenceManager: FingerprintPreferencesManager) : MatchTask {

    private val matchingIdentifyRequest = matchingRequest as MatchingTaskIdentifyRequest

    companion object {
        const val matchingEndWaitTimeInMillis = 1000
    }

    override val matchStartTime = timeHelper.now()

    override fun loadCandidates(): Single<List<Person>> =
        Completable.fromAction {
            viewModel.hasLoadingBegun.postValue(true)
            viewModel.progress.postValue(25)
        }.andThen(
            with(matchingIdentifyRequest.queryForIdentifyPool) {
                dbManager.loadPeople(this.projectId, this.userId, this.moduleId)
            }
        )

    override fun handlesCandidatesLoaded(candidates: List<Person>) {
        logMessageForCrashReport(String.format(Locale.UK,
            "Successfully loaded %d candidates", candidates.size))
        viewModel.matchBeginningSummary.postValue(MatchingViewModel.IdentificationBeginningSummary(candidates.size))
        viewModel.progress.postValue(50)
    }

    override fun getMatcherType(): LibMatcher.MATCHER_TYPE = LibMatcher.MATCHER_TYPE.SIMAFIS_IDENTIFY

    override fun onMatchProgressDo(progress: Int) {
        // Progress mapped from 0 to 100 to be between 50 and 100
        viewModel.progress.postValue(progress / 2 + 50)
    }

    override fun handleMatchResult(candidates: List<Person>, scores: List<Float>) {

        val topCandidates = candidates
            .zip(scores)
            .sortedByDescending { (_, score) -> score }
            .take(matchingIdentifyRequest.returnIdCount)
            .map { (candidate, score) ->
                MatchingResult(candidate.patientId, score.toInt(), MatchingTier.computeTier(score))
            }

        sessionEventsManager.addEventInBackground(
            OneToManyMatchEvent(
                matchStartTime,
                timeHelper.now(),
                OneToManyMatchEvent.MatchPool(MatchPoolType.fromQueryForIdentifyPool(matchingIdentifyRequest.queryForIdentifyPool), candidates.size),
                topCandidates.map { MatchEntry(it.guid, it.confidence.toFloat()) }))


        val tier1Or2Matches = topCandidates.count { (_, _, tier) -> tier == MatchingTier.TIER_1 || tier == MatchingTier.TIER_2 }
        val tier3Matches = topCandidates.count { (_, _, tier) -> tier == MatchingTier.TIER_3 }
        val tier4Matches = topCandidates.count { (_, _, tier) -> tier == MatchingTier.TIER_4 }

        preferenceManager.lastIdentificationDate = Date()

        val resultData = Intent().putExtra(MatchingTaskResult.BUNDLE_KEY,
            MatchingTaskIdentifyResult(topCandidates))

        viewModel.progress.postValue(100)

        viewModel.matchFinishedSummary.postValue(MatchingViewModel.IdentificationFinishedSummary(
            topCandidates.size, tier1Or2Matches, tier3Matches, tier4Matches
        ))
        viewModel.result.postValue(MatchingViewModel.FinishResult(
            ResultCode.OK, resultData, matchingEndWaitTimeInMillis
        ))
    }

    private fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(MATCHING, UI, Log.INFO, message)
    }
}
