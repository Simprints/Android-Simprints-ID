package com.simprints.fingerprint.activities.matching

import android.content.Intent
import android.util.Log
import com.simprints.fingerprint.activities.matching.request.MatchingTaskRequest
import com.simprints.fingerprint.activities.matching.result.MatchingTaskResult
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTag.MATCHING
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTrigger.UI
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.MatchEntry
import com.simprints.fingerprint.controllers.core.eventData.model.OneToOneMatchEvent
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.data.domain.fingerprint.FingerprintIdentity
import com.simprints.fingerprint.data.domain.matching.MatchResult
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import com.simprints.fingerprintmatcher.LibMatcher
import io.reactivex.Single
import java.util.*

class VerificationTask(private val viewModel: MatchingViewModel,
                       private val matchingRequest: MatchingTaskRequest,
                       private val dbManager: FingerprintDbManager,
                       private val sessionEventsManager: FingerprintSessionEventsManager,
                       private val crashReportManager: FingerprintCrashReportManager,
                       private val timeHelper: FingerprintTimeHelper) : MatchTask {

    override val matchStartTime = timeHelper.now()

    override fun loadCandidates(): Single<List<FingerprintIdentity>> =
        dbManager.loadPeople(matchingRequest.queryForCandidates)

    override fun handlesCandidatesLoaded(candidates: List<FingerprintIdentity>) {
        logMessageForCrashReport(String.format(Locale.UK,
            "Successfully loaded %d candidates", candidates.size))
    }

    override fun getMatcherType(): LibMatcher.MATCHER_TYPE = LibMatcher.MATCHER_TYPE.SIMAFIS_VERIFY

    override fun onMatchProgressDo(progress: Int) {
        viewModel.progress.postValue(100)
    }

    override fun handleMatchResult(candidates: List<FingerprintIdentity>, scores: List<Float>) {
        val candidate = candidates.first()
        val score = scores.first()

        val verificationResult = MatchEntry(candidate.personId, score)
        sessionEventsManager.addEventInBackground(OneToOneMatchEvent(
            matchStartTime,
            timeHelper.now(),
            matchingRequest.queryForCandidates,
            verificationResult))

        val resultData = Intent().putExtra(MatchingTaskResult.BUNDLE_KEY,
            MatchingTaskResult(listOf(MatchResult(candidate.personId, score))))

        viewModel.result.postValue(MatchingViewModel.FinishResult(ResultCode.OK, resultData, 0))
    }

    private fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(MATCHING, UI, Log.INFO, message)
    }
}
