package com.simprints.fingerprint.activities.matching

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.simprints.fingerprint.data.domain.matching.request.MatchingActIdentifyRequest
import com.simprints.fingerprint.data.domain.matching.request.MatchingActRequest
import com.simprints.fingerprint.data.domain.matching.result.MatchingActIdentifyResult
import com.simprints.fingerprint.data.domain.matching.result.MatchingActResult
import com.simprints.fingerprint.data.domain.matching.result.MatchingResult
import com.simprints.fingerprint.data.domain.matching.result.MatchingTier
import com.simprints.fingerprint.tools.utils.TimeHelper
import com.simprints.fingerprintmatcher.LibMatcher
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.fingerprint.Person
import com.simprints.id.domain.matching.IdentificationResult
import com.simprints.id.domain.matching.Tier
import io.reactivex.Completable
import io.reactivex.Single
import java.util.*

internal class IdentificationTask(private val view: MatchingContract.View,
                                  matchingRequest: MatchingActRequest,
                                  private val dbManager: DbManager,
                                  private val sessionEventsManager: SessionEventsManager,
                                  private val crashReportManager: CrashReportManager,
                                  timeHelper: TimeHelper) : MatchTask {

    private val matchingIdentifyRequest = matchingRequest as MatchingActIdentifyRequest

    companion object {
        const val matchingEndWaitTimeSeconds = 1 * 1000
    }

    override val matchStartTime = timeHelper.now()

    override fun loadCandidates(): Single<List<Person>> =
        Completable.fromAction { view.setIdentificationProgressLoadingStart() }
            .andThen(dbManager.loadPeople(GROUP.valueOf(matchingIdentifyRequest.matchGroup.name)))

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

        sessionEventsManager.addOneToManyEventInBackground(matchStartTime, topCandidates.map { IdentificationResult(it.guid, it.confidence, Tier.valueOf(it.tier.name)) }, candidates.size)

        val tier1Or2Matches = topCandidates.count { (_, _, tier) -> tier == MatchingTier.TIER_1 || tier == MatchingTier.TIER_2 }
        val tier3Matches = topCandidates.count { (_, _, tier) -> tier == MatchingTier.TIER_3 }
        val tier4Matches = topCandidates.count { (_, _, tier) -> tier == MatchingTier.TIER_4 }

        val resultData = Intent().putExtra(MatchingActResult.BUNDLE_KEY,
            MatchingActIdentifyResult(topCandidates))
        view.doSetResult(Activity.RESULT_OK, resultData)
        view.setIdentificationProgressFinished(topCandidates.size, tier1Or2Matches, tier3Matches, tier4Matches, matchingEndWaitTimeSeconds)
    }

    private fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(CrashReportTag.MATCHING, CrashReportTrigger.UI, Log.INFO, message)
    }
}
