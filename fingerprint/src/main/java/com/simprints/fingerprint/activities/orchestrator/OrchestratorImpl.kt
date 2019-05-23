package com.simprints.fingerprint.activities.orchestrator

import android.app.Activity
import android.content.Intent
import com.simprints.fingerprint.activities.alert.response.AlertActResult
import com.simprints.fingerprint.activities.alert.response.AlertActResult.CloseButtonAction.CLOSE
import com.simprints.fingerprint.activities.alert.response.AlertActResult.CloseButtonAction.TRY_AGAIN
import com.simprints.fingerprint.activities.orchestrator.OrchestratedActivity.ActivityName.*
import com.simprints.fingerprint.data.domain.collect.CollectFingerprintsActResult
import com.simprints.fingerprint.data.domain.matching.result.MatchingActIdentifyResult
import com.simprints.fingerprint.data.domain.matching.result.MatchingActResult
import com.simprints.fingerprint.data.domain.matching.result.MatchingActVerifyResult
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.DomainToFingerprintResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.*
import com.simprints.fingerprint.data.domain.refusal.RefusalActResult
import com.simprints.fingerprint.data.domain.refusal.RefusalActResult.Action.SCAN_FINGERPRINTS
import com.simprints.fingerprint.data.domain.refusal.RefusalActResult.Action.SUBMIT
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse

class OrchestratorImpl : Orchestrator {

    override fun onActivityResult(receiver: OrchestratedActivity, requestCode: Int, resultCode: Int?, data: Intent?) {

        extractAlertActivityResponse(data)?.let {
            when (it.closeButtonAction) {
                CLOSE -> {
                    // LaunchAct returns a FingerprintResponse to the AppModule, otherwise any Act forward back the result.
                    if (receiver.activity == LAUNCH) {
                        receiver.setResultDataAndFinish(Activity.RESULT_OK, prepareErrorResponse(it))
                    } else {
                        receiver.setResultDataAndFinish(resultCode, data)
                    }
                }
                TRY_AGAIN -> receiver.tryAgain()
            }
        } ?: run {
            when (receiver.activity) {
                COLLECT -> handleResultForCollectActivity(receiver, requestCode, resultCode, data)
                LAUNCH -> handleResultForLaunchActivity(receiver, requestCode, resultCode, data)
                MATCHING -> { handleResultForMatchingActivity(receiver, requestCode, resultCode, data) }
            }
        }
    }

    private fun handleResultForMatchingActivity(receiver: OrchestratedActivity,
                                                requestCode: Int, resultCode: Int?, data: Intent?) {

        //Default behavior - E.g. RESULT_CANCEL (backButton)
        forwardResultBack(receiver, resultCode, data)
    }

    private fun handleResultForCollectActivity(receiver: OrchestratedActivity,
                                               requestCode: Int, resultCode: Int?, data: Intent?) {

        extractRefusalActResult(data)?.let {
            when (it.action) {
                SUBMIT -> receiver.setResultDataAndFinish(resultCode, data)
                SCAN_FINGERPRINTS -> { /* Do Nothing */
                }
            }
            return
        }

        //Default behavior - E.g. MatchinAct or RESULT_CANCEL (backButton)
        forwardResultBack(receiver, resultCode, data)
    }

    private fun handleResultForLaunchActivity(receiver: OrchestratedActivity,
                                              requestCode: Int, resultCode: Int?, data: Intent?) {

        extractRefusalActResult(data)?.let {
            when (it.action) {
                SUBMIT -> receiver.setResultDataAndFinish(resultCode, prepareRefusalForm(it))
                SCAN_FINGERPRINTS -> { /* Do Nothing */
                }
            }
            return
        }

        //From CollectAct: for Enrolment
        extractCollectActivityResponse(data)?.let {
            receiver.setResultDataAndFinish(resultCode, prepareEnrolResponseIntent(it))
            return
        }

        //From MatchingAct: for Identification and Verification
        extractMatchingActResult(data)?.let {
            when (it) {
                is MatchingActVerifyResult -> prepareVerifyResponseIntent(it)
                is MatchingActIdentifyResult -> prepareIdentifyResponseIntent(it)
                else -> null
            }
        }?.let {
            receiver.setResultDataAndFinish(Activity.RESULT_OK, it)
            return
        }

        //Default behavior - E.g. RESULT_CANCEL (backButton)
        forwardResultBack(receiver, resultCode, data)
    }

    private fun forwardResultBack(receiver: OrchestratedActivity, resultCode: Int?, data: Intent?) {
        receiver.setResultDataAndFinish(resultCode, data)
    }

    private fun prepareErrorResponse(alertActResult: AlertActResult) =
        Intent().apply {
            val fingerprintErrorResponse = FingerprintErrorReason.fromFingerprintAlertToErrorResponse(alertActResult.alert)
            putExtra(IFingerprintResponse.BUNDLE_KEY,
                DomainToFingerprintResponse.fromDomainToFingerprintErrorResponse(fingerprintErrorResponse))
        }

    private fun prepareRefusalForm(refusalForm: RefusalActResult) =
        Intent().apply {
            val fingerprintResult = FingerprintRefusalFormResponse(
                refusalForm.answer?.reason.toString(),
                refusalForm.answer?.optionalText.toString())

            putExtra(IFingerprintResponse.BUNDLE_KEY,
                DomainToFingerprintResponse.fromDomainToFingerprintRefusalFormResponse(fingerprintResult))
        }

    private fun prepareVerifyResponseIntent(matchVerifyResult: MatchingActVerifyResult) =
        Intent().apply {
            val fingerprintResult = FingerprintVerifyResponse(
                matchVerifyResult.guid,
                matchVerifyResult.confidence,
                matchVerifyResult.tier)

            putExtra(IFingerprintResponse.BUNDLE_KEY,
                DomainToFingerprintResponse.fromDomainToFingerprintVerifyResponse(fingerprintResult))
        }

    private fun prepareIdentifyResponseIntent(matchIdentifyResult: MatchingActIdentifyResult) =
        Intent().apply {
            val fingerprintResult = FingerprintIdentifyResponse(matchIdentifyResult.identifications)
            putExtra(IFingerprintResponse.BUNDLE_KEY,
                DomainToFingerprintResponse.fromDomainToFingerprintIdentifyResponse(fingerprintResult))
        }

    private fun prepareEnrolResponseIntent(collectFingerprintsActResult: CollectFingerprintsActResult) =
        Intent().apply {
            val fingerprintResult = FingerprintEnrolResponse(collectFingerprintsActResult.probe.patientId)
            putExtra(IFingerprintResponse.BUNDLE_KEY, DomainToFingerprintResponse.fromDomainToFingerprintEnrolResponse(fingerprintResult))
        }

    private fun extractAlertActivityResponse(data: Intent?): AlertActResult? =
        data?.getParcelableExtra(AlertActResult.BUNDLE_KEY)

    private fun extractRefusalActResult(data: Intent?): RefusalActResult? =
        data?.getParcelableExtra(RefusalActResult.BUNDLE_KEY)

    private fun extractMatchingActResult(data: Intent?): MatchingActResult? =
        data?.getParcelableExtra(MatchingActResult.BUNDLE_KEY)

    private fun extractCollectActivityResponse(data: Intent?): CollectFingerprintsActResult? =
        data?.getParcelableExtra(CollectFingerprintsActResult.BUNDLE_KEY)
}

