package com.simprints.fingerprint.activities.matching

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.simprints.fingerprint.data.domain.requests.FingerprintRequest
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.fingerprint.Person
import com.simprints.id.domain.matching.IdentificationResult
import com.simprints.id.domain.matching.Tier
import com.simprints.id.domain.responses.IdentifyResponse
import com.simprints.id.domain.responses.Response
import com.simprints.id.tools.TimeHelper
import com.simprints.fingerprintmatcher.LibMatcher
import io.reactivex.Completable
import io.reactivex.Single
import java.util.*

internal class IdentificationTask(private val view: MatchingContract.View,
                                  private val dbManager: DbManager,
                                  private val preferencesManager: PreferencesManager,
                                  private val sessionEventsManager: SessionEventsManager,
                                  private val crashReportManager: CrashReportManager,
                                  timeHelper: TimeHelper) : MatchTask {

    override val matchStartTime = timeHelper.now()

    override fun loadCandidates(fingerprintRequest: FingerprintRequest): Single<List<Person>> =
        Completable.fromAction { view.setIdentificationProgressLoadingStart() }
            .andThen(dbManager.loadPeople(preferencesManager.matchGroup))

    override fun handlesCandidatesLoaded(candidates: List<Person>) {
        logMessageForCrashReport(String.format(Locale.UK,
            "Successfully loaded %d candidates", candidates.size))
        view.setIdentificationProgressMatchingStart(candidates.size)
    }

    override fun getMatcherType(): LibMatcher.MATCHER_TYPE =
        when (preferencesManager.matcherType) {
            0 -> LibMatcher.MATCHER_TYPE.SIMAFIS_IDENTIFY
            1 -> LibMatcher.MATCHER_TYPE.SOURCEAFIS_IDENTIFY
            else -> LibMatcher.MATCHER_TYPE.SIMAFIS_IDENTIFY
        }

    override fun onMatchProgressDo(progress: Int) {
        view.setIdentificationProgress(progress)
    }

    override fun handleMatchResult(candidates: List<Person>, scores: List<Float>) {
        view.setIdentificationProgressReturningStart()

        val topCandidates = candidates
            .zip(scores)
            .sortedByDescending { (_, score) -> score }
            .take(preferencesManager.returnIdCount)
            .map { (candidate, score) ->
                IdentificationResult(candidate.patientId, score.toInt(), Tier.computeTier(score))
            }

        sessionEventsManager.addOneToManyEventInBackground(matchStartTime, topCandidates, candidates.size)

        val tier1Or2Matches = topCandidates.count { (_, _, tier) -> tier == Tier.TIER_1 || tier == Tier.TIER_2 }
        val tier3Matches = topCandidates.count { (_, _, tier) -> tier == Tier.TIER_3 }
        val tier4Matches = topCandidates.count { (_, _, tier) -> tier == Tier.TIER_4 }

        val resultData = Intent().putExtra(Response.BUNDLE_KEY,
            IdentifyResponse(topCandidates, getCurrentSessionId()))
        view.doSetResult(Activity.RESULT_OK, resultData)
        view.setIdentificationProgressFinished(topCandidates.size, tier1Or2Matches, tier3Matches, tier4Matches, preferencesManager.matchingEndWaitTimeSeconds * 1000)
    }

    private fun getCurrentSessionId() = ""
    // sessionEventsManager.getCurrentSession().map { it.id }.blockingGet() STOPSHIP

    private fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(CrashReportTag.MATCHING, CrashReportTrigger.UI, Log.INFO, message)
    }
}
