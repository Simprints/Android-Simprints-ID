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
import com.simprints.fingerprint.controllers.core.eventData.model.Matcher
import com.simprints.fingerprint.controllers.core.eventData.model.OneToOneMatchEvent
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.data.domain.matching.MatchResult
import com.simprints.fingerprint.orchestrator.domain.ResultCode

class VerificationTask(private val viewModel: MatchingViewModel,
                       private val matchingRequest: MatchingTaskRequest,
                       private val sessionEventsManager: FingerprintSessionEventsManager,
                       private val crashReportManager: FingerprintCrashReportManager,
                       private val timeHelper: FingerprintTimeHelper) : MatchTask {

    override val matchStartTime = timeHelper.now()

    override fun onBeginLoadCandidates() {
    }

    override fun onCandidatesLoaded(numberOfCandidates: Int) {
        logMessageForCrashReport("Successfully loaded $numberOfCandidates candidates")
    }

    override fun handleMatchResult(numberOfCandidates: Int, matchResults: List<MatchResult>) {
        val matchResult = matchResults.first()

        val verificationResult = MatchEntry(matchResult.guid, matchResult.confidence)
        sessionEventsManager.addEventInBackground(OneToOneMatchEvent(
            matchStartTime,
            timeHelper.now(),
            matchingRequest.queryForCandidates,
            Matcher.SIM_AFIS,
            verificationResult))

        val resultData = Intent().putExtra(MatchingTaskResult.BUNDLE_KEY,
            MatchingTaskResult(listOf(matchResult)))

        viewModel.progress.postValue(100)
        viewModel.result.postValue(MatchingViewModel.FinishResult(ResultCode.OK, resultData, 0))
    }

    private fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(MATCHING, UI, Log.INFO, message)
    }
}
