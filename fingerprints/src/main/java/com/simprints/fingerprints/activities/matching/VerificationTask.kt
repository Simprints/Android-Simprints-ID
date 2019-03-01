package com.simprints.fingerprints.activities.matching

import android.app.Activity
import android.content.Intent
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.fingerprint.Person
import com.simprints.id.domain.matching.Tier
import com.simprints.id.domain.matching.VerificationResult
import com.simprints.id.domain.requests.Request
import com.simprints.id.domain.requests.VerifyRequest
import com.simprints.id.domain.responses.Response
import com.simprints.id.domain.responses.VerifyResponse
import com.simprints.id.domain.responses.toClientApiVerifyResponse
import com.simprints.id.tools.TimeHelper
import com.simprints.libmatcher.LibMatcher
import io.reactivex.Single
import java.util.*

internal class VerificationTask(view: MatchingContract.View,
                                dbManager: DbManager,
                                preferencesManager: PreferencesManager,
                                sessionEventsManager: SessionEventsManager,
                                crashReportManager: CrashReportManager,
                                timeHelper: TimeHelper)
    : MatchTask(view, dbManager, preferencesManager, sessionEventsManager, crashReportManager, timeHelper) {

    override fun loadCandidates(appRequest: Request): Single<List<Person>> =
        dbManager.loadPerson((appRequest as VerifyRequest).verifyGuid).map { listOf(it.person) }

    override fun handlesCandidatesLoaded(candidates: List<Person>) {
        logMessageForCrashReport(String.format(Locale.UK,
            "Successfully loaded %d candidates", candidates.size))
    }

    override fun getMatcherType(): LibMatcher.MATCHER_TYPE =
        when (preferencesManager.matcherType) {
            0 -> LibMatcher.MATCHER_TYPE.SIMAFIS_VERIFY
            1 -> LibMatcher.MATCHER_TYPE.SOURCEAFIS_VERIFY
            else -> LibMatcher.MATCHER_TYPE.SIMAFIS_VERIFY
        }

    override fun onMatchProgressDo(progress: Int) {}

    override fun handleMatchResult(candidates: List<Person>, scores: List<Float>) {
        val candidate = candidates.first()
        val score = scores.first()

        val verificationResult = VerificationResult(candidate.patientId, score.toInt(), Tier.computeTier(score))

        sessionEventsManager.addOneToOneMatchEventInBackground(candidates.first().patientId, matchStartTime, verificationResult)
        val resultData = Intent().putExtra(Response.BUNDLE_KEY,
            VerifyResponse(candidate.patientId, score.toInt(), Tier.computeTier(score)).toClientApiVerifyResponse())
        view.doSetResult(Activity.RESULT_OK, resultData)
        view.doFinish()
    }
}
