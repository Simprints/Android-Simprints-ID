package com.simprints.fingerprint.activities.matching

import android.annotation.SuppressLint
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.activities.matching.request.MatchingTaskRequest
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.flow.Action
import com.simprints.fingerprint.controllers.core.flow.MasterFlowManager
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier
import com.simprints.fingerprint.data.domain.fingerprint.Fingerprint
import com.simprints.fingerprint.data.domain.fingerprint.FingerprintIdentity
import com.simprints.fingerprint.data.domain.matching.MatchResult
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import com.simprints.fingerprintmatcher.FingerprintMatcher
import com.simprints.fingerprintmatcher.domain.MatchingAlgorithm
import com.simprints.fingerprintmatcher.domain.TemplateFormat
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import java.util.*
import com.simprints.fingerprintmatcher.domain.FingerIdentifier as MatcherFingerIdentifier
import com.simprints.fingerprintmatcher.domain.Fingerprint as MatcherFingerprint
import com.simprints.fingerprintmatcher.domain.FingerprintIdentity as MatcherFingerprintIdentity
import com.simprints.fingerprintmatcher.domain.MatchResult as MatcherMatchResult


class MatchingViewModel(
    private val fingerprintMatcher: FingerprintMatcher,
    private val dbManager: FingerprintDbManager,
    private val sessionEventsManager: FingerprintSessionEventsManager,
    private val timeHelper: FingerprintTimeHelper,
    private val masterFlowManager: MasterFlowManager,
    private val fingerprintPreferencesManager: FingerprintPreferencesManager
) : ViewModel() {

    val result = MutableLiveData<FinishResult>()
    val progress = MutableLiveData(0)
    val alert = MutableLiveData<FingerprintAlert>()
    val hasLoadingBegun = MutableLiveData<Boolean>()
    val matchBeginningSummary = MutableLiveData<IdentificationBeginningSummary>()
    val matchFinishedSummary = MutableLiveData<IdentificationFinishedSummary>()
    val hasMatchFailed = MutableLiveData<Boolean>()
    private val isCrossFingerMatchingEnabledInVerification:Boolean
        get() = fingerprintPreferencesManager.isCrossFingerComparisonEnabledInVerification
    private lateinit var matchingRequest: MatchingTaskRequest

    @SuppressLint("CheckResult")
    fun start(matchingRequest: MatchingTaskRequest) {
        this.matchingRequest = matchingRequest

        //Will be deprecated. The matching will have to run irrespective of the action from the calling app.
        when (masterFlowManager.getCurrentAction()) {
            Action.ENROL, Action.IDENTIFY -> runMatchTask(IdentificationTask(
                this,
                matchingRequest,
                sessionEventsManager,
                timeHelper
            ),false)
            Action.VERIFY -> runMatchTask(
                VerificationTask(
                    this,
                    matchingRequest,
                    sessionEventsManager,
                    timeHelper
                ),isCrossFingerMatchingEnabledInVerification
            )
        }
    }

    private fun runMatchTask(matchTask: MatchTask, isCrossFingerMatchingEnabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                with(matchTask) {
                    onBeginLoadCandidates()

                    val candidates = dbManager.loadPeople(matchingRequest.queryForCandidates)
                    onCandidatesLoaded(candidates.count())

                    val result = runMatch(
                        candidates.toList(), matchingRequest.probeFingerprintSamples,
                        isCrossFingerMatchingEnabled
                    )
                    handleMatchResult(
                        candidates.count(),
                        result.toList(),
                        isCrossFingerMatchingEnabled
                    )
                }
            } catch (e: Throwable) {
                handleMatchFailed(e)
            }
        }
    }

    private fun runMatch(
        candidates: List<FingerprintIdentity>,
        probeFingerprints: List<Fingerprint>,
        isCrossFingerMatchingEnabled: Boolean
    ): List<MatchResult> =
        fingerprintMatcher.match(
            probeFingerprints.toFingerprintIdentity().fromDomainToMatcher(),
            candidates.map { it.fromDomainToMatcher() },
            DEFAULT_MATCHING_ALGORITHM, isCrossFingerMatchingEnabled ,
        ).map { it.fromMatcherToDomain() }

    private fun handleMatchFailed(e: Throwable) {
        Simber.e(e)
        hasMatchFailed.postValue(true)
        result.postValue(FinishResult(ResultCode.CANCELLED, null, 0))
    }

    private fun List<Fingerprint>.toFingerprintIdentity(
        id: String = UUID.randomUUID().toString()
    ): FingerprintIdentity = FingerprintIdentity(id, this)

    private fun FingerprintIdentity.fromDomainToMatcher(): MatcherFingerprintIdentity =
        MatcherFingerprintIdentity(personId, fingerprints.map { it.fromDomainToMatcher() })

    private fun Fingerprint.fromDomainToMatcher(): MatcherFingerprint =
        MatcherFingerprint(fingerId.fromDomainToMatcher(), templateBytes, SAVED_TEMPLATE_FORMAT)

    private fun FingerIdentifier.fromDomainToMatcher(): MatcherFingerIdentifier =
        when (this) {
            FingerIdentifier.RIGHT_5TH_FINGER -> MatcherFingerIdentifier.RIGHT_5TH_FINGER
            FingerIdentifier.RIGHT_4TH_FINGER -> MatcherFingerIdentifier.RIGHT_4TH_FINGER
            FingerIdentifier.RIGHT_3RD_FINGER -> MatcherFingerIdentifier.RIGHT_3RD_FINGER
            FingerIdentifier.RIGHT_INDEX_FINGER -> MatcherFingerIdentifier.RIGHT_INDEX_FINGER
            FingerIdentifier.RIGHT_THUMB -> MatcherFingerIdentifier.RIGHT_THUMB
            FingerIdentifier.LEFT_THUMB -> MatcherFingerIdentifier.LEFT_THUMB
            FingerIdentifier.LEFT_INDEX_FINGER -> MatcherFingerIdentifier.LEFT_INDEX_FINGER
            FingerIdentifier.LEFT_3RD_FINGER -> MatcherFingerIdentifier.LEFT_3RD_FINGER
            FingerIdentifier.LEFT_4TH_FINGER -> MatcherFingerIdentifier.LEFT_4TH_FINGER
            FingerIdentifier.LEFT_5TH_FINGER -> MatcherFingerIdentifier.LEFT_5TH_FINGER
        }

    private fun MatcherMatchResult.fromMatcherToDomain(): MatchResult =
        MatchResult(id, score)

    data class IdentificationBeginningSummary(val matchSize: Int)

    data class IdentificationFinishedSummary(
        val returnSize: Int,
        val veryGoodMatches: Int,
        val goodMatches: Int,
        val fairMatches: Int
    )

    data class FinishResult(
        val resultCode: ResultCode,
        val data: Intent?,
        val finishDelayMillis: Int
    )

    companion object {
        val SAVED_TEMPLATE_FORMAT = TemplateFormat.ISO_19794_2_2011
        val DEFAULT_MATCHING_ALGORITHM = MatchingAlgorithm.SIM_AFIS
    }
}
