package com.simprints.fingerprint.activities.matching

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTag.MATCHING
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTrigger.UI
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.MatchEntry
import com.simprints.fingerprint.controllers.core.eventData.model.OneToManyMatchEvent
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.data.domain.matching.request.MatchingActIdentifyRequest
import com.simprints.fingerprint.data.domain.matching.request.MatchingActRequest
import com.simprints.fingerprint.data.domain.matching.result.MatchingActIdentifyResult
import com.simprints.fingerprint.data.domain.matching.result.MatchingActResult
import com.simprints.fingerprint.data.domain.matching.result.MatchingResult
import com.simprints.fingerprint.data.domain.matching.result.MatchingTier
import com.simprints.fingerprint.data.domain.person.Person
import com.simprints.fingerprintmatcher.LibMatcher
import io.reactivex.Completable
import io.reactivex.Single
import java.util.*

internal class IdentificationTask(private val view: MatchingContract.View,
                                  matchingRequest: MatchingActRequest,
                                  private val dbManager: FingerprintDbManager,
                                  private val sessionEventsManager: FingerprintSessionEventsManager,
                                  private val crashReportManager: FingerprintCrashReportManager,
                                  private val timeHelper: FingerprintTimeHelper,
                                  private val preferenceManager: FingerprintPreferencesManager) : MatchTask {

    private val matchingIdentifyRequest = matchingRequest as MatchingActIdentifyRequest

    companion object {
        const val matchingEndWaitTimeInMillis = 1000
    }

    override val matchStartTime = timeHelper.now()

    override fun loadCandidates(): Single<List<Person>> =
        Completable.fromAction { view.setIdentificationProgressLoadingStart() }
            .andThen(dbManager.loadPeople(matchingIdentifyRequest.matchGroup))

    override fun handlesCandidatesLoaded(candidates: List<Person>) {
        logMessageForCrashReport(String.format(Locale.UK,
            "Successfully loaded %d candidates", candidates.size))
        view.setIdentificationProgressMatchingStart(candidates.size)
    }

    override fun getMatcherType(): LibMatcher.MATCHER_TYPE = LibMatcher.MATCHER_TYPE.SIMAFIS_IDENTIFY

    override fun onMatchProgressDo(progress: Int) {
        view.setIdentificationProgress(progress)
    }

    override fun handleMatchResult(candidates: List<Person>, scores: List<Float>) {
        view.setIdentificationProgressReturningStart()

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
                OneToManyMatchEvent.MatchPool(preferenceManager.matchPoolType, candidates.size),
                topCandidates.map { MatchEntry(it.guid, it.confidence.toFloat()) }))


        val tier1Or2Matches = topCandidates.count { (_, _, tier) -> tier == MatchingTier.TIER_1 || tier == MatchingTier.TIER_2 }
        val tier3Matches = topCandidates.count { (_, _, tier) -> tier == MatchingTier.TIER_3 }
        val tier4Matches = topCandidates.count { (_, _, tier) -> tier == MatchingTier.TIER_4 }

        preferenceManager.lastIdentificationDate = Date()

        val resultData = Intent().putExtra(MatchingActResult.BUNDLE_KEY,
            MatchingActIdentifyResult(topCandidates))
        view.doSetResult(Activity.RESULT_OK, resultData)
        view.setIdentificationProgressFinished(topCandidates.size, tier1Or2Matches, tier3Matches, tier4Matches, matchingEndWaitTimeInMillis)
    }

    private fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(MATCHING, UI, Log.INFO, message)
    }
}
