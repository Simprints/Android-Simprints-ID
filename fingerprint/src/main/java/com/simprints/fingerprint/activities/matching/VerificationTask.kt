package com.simprints.fingerprint.activities.matching

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTag.MATCHING
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTrigger.UI
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.MatchEntry
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.data.domain.matching.request.MatchingActRequest
import com.simprints.fingerprint.data.domain.matching.request.MatchingActVerifyRequest
import com.simprints.fingerprint.data.domain.matching.result.MatchingActResult
import com.simprints.fingerprint.data.domain.matching.result.MatchingActVerifyResult
import com.simprints.fingerprint.data.domain.matching.result.MatchingTier
import com.simprints.fingerprintmatcher.LibMatcher
import com.simprints.id.domain.fingerprint.Person
import io.reactivex.Single
import java.util.*

internal class VerificationTask(private val view: MatchingContract.View,
                                matchingRequest: MatchingActRequest,
                                private val dbManager: FingerprintDbManager,
                                private val sessionEventsManager: FingerprintSessionEventsManager,
                                private val crashReportManager: FingerprintCrashReportManager,
                                timeHelper: FingerprintTimeHelper) : MatchTask {

    private val matchingVerifyRequest = matchingRequest as MatchingActVerifyRequest

    override val matchStartTime = timeHelper.now()

    override fun loadCandidates(): Single<List<Person>> =
        dbManager.loadPerson(matchingVerifyRequest.projectId, matchingVerifyRequest.verifyGuid).map { listOf(it.person) }

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

        sessionEventsManager.addOneToOneMatchEventInBackground(candidates.first().patientId, matchStartTime, verificationResult)
        val resultData = Intent().putExtra(MatchingActResult.BUNDLE_KEY,
            MatchingActVerifyResult(candidate.patientId, score.toInt(), MatchingTier.computeTier(score)))
        view.doSetResult(Activity.RESULT_OK, resultData)
        view.doFinish()
    }

    private fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(MATCHING, UI, Log.INFO, message)
    }
}
