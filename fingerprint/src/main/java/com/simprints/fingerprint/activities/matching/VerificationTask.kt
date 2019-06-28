package com.simprints.fingerprint.activities.matching

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTag.MATCHING
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTrigger.UI
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.MatchEntry
import com.simprints.fingerprint.controllers.core.eventData.model.OneToOneMatchEvent
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.activities.matching.request.MatchingActRequest
import com.simprints.fingerprint.activities.matching.request.MatchingActVerifyRequest
import com.simprints.fingerprint.activities.matching.result.MatchingActResult
import com.simprints.fingerprint.activities.matching.result.MatchingActVerifyResult
import com.simprints.fingerprint.data.domain.matching.MatchingTier
import com.simprints.fingerprintmatcher.LibMatcher
import com.simprints.fingerprint.data.domain.person.Person
import io.reactivex.Single
import java.util.*

internal class VerificationTask(private val view: MatchingContract.View,
                                matchingRequest: MatchingActRequest,
                                private val dbManager: FingerprintDbManager,
                                private val sessionEventsManager: FingerprintSessionEventsManager,
                                private val crashReportManager: FingerprintCrashReportManager,
                                private val timeHelper: FingerprintTimeHelper,
                                private val preferenceManager: FingerprintPreferencesManager) : MatchTask {

    private val matchingVerifyRequest = matchingRequest as MatchingActVerifyRequest

    override val matchStartTime = timeHelper.now()

    override fun loadCandidates(): Single<List<Person>> =
        with(matchingVerifyRequest) {
            dbManager.loadPerson(this.queryForVerifyPool.projectId, this.verifyGuid).map { listOf(it.person) }
        }

    override fun handlesCandidatesLoaded(candidates: List<Person>) {
        logMessageForCrashReport(String.format(Locale.UK,
            "Successfully loaded %d candidates", candidates.size))
    }

    override fun getMatcherType(): LibMatcher.MATCHER_TYPE = LibMatcher.MATCHER_TYPE.SIMAFIS_VERIFY

    override fun onMatchProgressDo(progress: Int) {
        view.setVerificationProgress()
    }

    override fun handleMatchResult(candidates: List<Person>, scores: List<Float>) {
        val candidate = candidates.first()
        val score = scores.first()

        val verificationResult = MatchEntry(candidate.patientId, score)
        sessionEventsManager.addEventInBackground(OneToOneMatchEvent(
            matchStartTime,
            timeHelper.now(),
            candidates.first().patientId,
            verificationResult))

        val resultData = Intent().putExtra(MatchingActResult.BUNDLE_KEY,
            MatchingActVerifyResult(candidate.patientId, score.toInt(), MatchingTier.computeTier(score)))

        preferenceManager.lastVerificationDate = Date()
        view.doSetResult(Activity.RESULT_OK, resultData)
        view.doFinish()
    }

    private fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(MATCHING, UI, Log.INFO, message)
    }
}
