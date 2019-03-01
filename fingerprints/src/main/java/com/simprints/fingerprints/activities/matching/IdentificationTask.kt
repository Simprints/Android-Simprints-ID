package com.simprints.fingerprints.activities.matching

import android.app.Activity
import android.content.Intent
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.fingerprint.Person
import com.simprints.id.domain.matching.IdentificationResult
import com.simprints.id.domain.matching.Tier
import com.simprints.id.domain.requests.Request
import com.simprints.id.domain.responses.IdentificationResponse
import com.simprints.id.domain.responses.Response
import com.simprints.id.domain.responses.toClientApiIdentifyResponse
import com.simprints.id.tools.TimeHelper
import com.simprints.libmatcher.LibMatcher
import io.reactivex.Single
import java.util.*

internal class IdentificationTask(view: MatchingContract.View,
                                  dbManager: DbManager,
                                  preferencesManager: PreferencesManager,
                                  sessionEventsManager: SessionEventsManager,
                                  crashReportManager: CrashReportManager,
                                  timeHelper: TimeHelper)
    : MatchTask(view, dbManager, preferencesManager, sessionEventsManager, crashReportManager, timeHelper) {

    override fun loadCandidates(appRequest: Request): Single<List<Person>> =
        dbManager.loadPeople(preferencesManager.matchGroup)

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
            IdentificationResponse(topCandidates, getCurrentSessionId()).toClientApiIdentifyResponse())
        view.doSetResult(Activity.RESULT_OK, resultData)
        view.setIdentificationProgressFinished(topCandidates.size, tier1Or2Matches, tier3Matches, tier4Matches, preferencesManager.matchingEndWaitTimeSeconds * 1000)
    }

    private fun getCurrentSessionId() =
        sessionEventsManager.getCurrentSession().map { it.id }.blockingGet()
}
