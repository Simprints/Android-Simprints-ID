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
import com.simprints.fingerprint.controllers.core.eventData.model.OneToManyMatchEvent
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.data.domain.matching.MatchResult
import com.simprints.fingerprint.orchestrator.domain.ResultCode

class IdentificationTask(private val viewModel: MatchingViewModel,
                         private val matchingRequest: MatchingTaskRequest,
                         private val sessionEventsManager: FingerprintSessionEventsManager,
                         private val crashReportManager: FingerprintCrashReportManager,
                         private val timeHelper: FingerprintTimeHelper) : MatchTask {

    override val matchStartTime = timeHelper.now()

    override fun onBeginLoadCandidates() {
        viewModel.hasLoadingBegun.postValue(true)
        viewModel.progress.postValue(25)
    }

    override fun onCandidatesLoaded(numberOfCandidates: Int) {
        logMessageForCrashReport("Successfully loaded $numberOfCandidates candidates")
        viewModel.matchBeginningSummary.postValue(MatchingViewModel.IdentificationBeginningSummary(numberOfCandidates))
        viewModel.progress.postValue(50)
    }

    override fun handleMatchResult(numberOfCandidates: Int, matchResults: List<MatchResult>) {
        val topCandidates = extractTopCandidates(matchResults)

        saveOneToManyMatchEvent(numberOfCandidates, topCandidates)

        postMatchFinishedSummary(topCandidates)
        postFinalProgress()
        postResultData(topCandidates)
    }

    private fun extractTopCandidates(matchResults: List<MatchResult>): List<MatchResult> =
        matchResults
            .sortedByDescending { it.confidence }
            .take(returnIdCount)

    private fun saveOneToManyMatchEvent(candidateSize: Int, topCandidates: List<MatchResult>) {
        sessionEventsManager.addEventInBackground(
            OneToManyMatchEvent(
                matchStartTime,
                timeHelper.now(),
                matchingRequest.queryForCandidates,
                candidateSize,
                Matcher.SIM_AFIS,
                topCandidates.map { MatchEntry(it.guid, it.confidence) }))
    }

    private fun postMatchFinishedSummary(topCandidates: List<MatchResult>) {
        val veryGoodMatches = topCandidates.count { (_, score) -> veryGoodMatchThreshold <= score }
        val goodMatches = topCandidates.count { (_, score) -> goodMatchThreshold <= score && score < veryGoodMatchThreshold }
        val fairMatches = topCandidates.count { (_, score) -> fairMatchThreshold <= score && score < goodMatchThreshold }
        viewModel.matchFinishedSummary.postValue(MatchingViewModel.IdentificationFinishedSummary(
            topCandidates.size, veryGoodMatches, goodMatches, fairMatches
        ))
    }

    private fun postFinalProgress() {
        viewModel.progress.postValue(100)
    }

    private fun postResultData(topCandidates: List<MatchResult>) {
        val resultData = Intent().putExtra(MatchingTaskResult.BUNDLE_KEY,
            MatchingTaskResult(topCandidates))

        viewModel.result.postValue(MatchingViewModel.FinishResult(
            ResultCode.OK, resultData, matchingEndWaitTimeInMillis
        ))
    }

    private fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(MATCHING, UI, Log.INFO, message)
    }

    companion object {
        const val matchingEndWaitTimeInMillis = 1000
        const val returnIdCount = 10

        const val veryGoodMatchThreshold = 50.0
        const val goodMatchThreshold = 35.0
        const val fairMatchThreshold = 20.0
    }
}
