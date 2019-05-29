package com.simprints.fingerprint.activities.orchestrator

import android.app.Activity
import android.content.Intent
import com.simprints.fingerprint.activities.alert.response.AlertActResult
import com.simprints.fingerprint.activities.alert.response.AlertActResult.CloseButtonAction.*
import com.simprints.fingerprint.activities.collect.CollectFingerprintsActivity
import com.simprints.fingerprint.activities.launch.LaunchActivity
import com.simprints.fingerprint.activities.matching.MatchingActivity
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

    override fun onActivityResult(receiver: OrchestratorCallback, requestCode: Int, resultCode: Int?, data: Intent?) {
        receiver.onActivityResultReceived()

        extractAlertActivityResult(data)?.let {
            if (receiver is LaunchActivity) {
                handleAlertScreenResultInLaunchAct(it, receiver)
            } else {
                handleAlertScreenResult(it, receiver, requestCode, data)
            }
        } ?: run {
            when (receiver) {
                is CollectFingerprintsActivity -> handleResultInCollectActivity(receiver, requestCode, data)
                is LaunchActivity -> handleResultInLaunchActivity(receiver, requestCode, data)
                is MatchingActivity -> { handleResultInMatchingActivity(receiver, requestCode, data) }
            }
        }
    }

    private fun handleAlertScreenResultInLaunchAct(alertActResult: AlertActResult,
                                        receiver: OrchestratorCallback) {
        when (alertActResult.closeButtonAction) {
            CLOSE -> {
                receiver.setResultDataAndFinish(Activity.RESULT_OK, prepareErrorResponse(alertActResult))
            } BACK -> {
                receiver.setResultDataAndFinish(Activity.RESULT_CANCELED, null)
            }
            TRY_AGAIN -> receiver.tryAgain()
        }
    }

    private fun handleAlertScreenResult(alertActResult: AlertActResult,
                                        receiver: OrchestratorCallback,
                                        resultCode: Int?,
                                        data: Intent?) {

        when (alertActResult.closeButtonAction) {
            CLOSE, BACK -> {
                forwardResultBack(receiver, resultCode, data)
            }
            TRY_AGAIN -> receiver.tryAgain()
        }
    }

    private fun handleResultInMatchingActivity(receiver: OrchestratorCallback,
                                               resultCode: Int?, data: Intent?) {

        //Default behavior - E.g. RESULT_CANCEL (backButton)
        forwardResultBack(receiver, resultCode, data)
    }

    private fun handleResultInCollectActivity(receiver: OrchestratorCallback,
                                              resultCode: Int?, data: Intent?) {

        extractRefusalActResult(data)?.let {
            when (it.action) {
                SUBMIT -> receiver.setResultDataAndFinish(resultCode, data)
                SCAN_FINGERPRINTS -> { /* Do Nothing */ }
            }
            return
        }

        //Default behavior - E.g. MatchinAct or RESULT_CANCEL (backButton)
        forwardResultBack(receiver, resultCode, data)
    }

    private fun handleResultInLaunchActivity(receiver: OrchestratorCallback,
                                             resultCode: Int?,
                                             data: Intent?) {

        extractRefusalActResult(data)?.let {
            when (it.action) {
                SUBMIT -> receiver.setResultDataAndFinish(resultCode, prepareRefusalForm(it))
                SCAN_FINGERPRINTS -> { receiver.tryAgain() }
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

    private fun forwardResultBack(receiver: OrchestratorCallback, resultCode: Int?, data: Intent? = null) {
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

    private fun extractAlertActivityResult(data: Intent?): AlertActResult? =
        data?.getParcelableExtra(AlertActResult.BUNDLE_KEY)

    private fun extractRefusalActResult(data: Intent?): RefusalActResult? =
        data?.getParcelableExtra(RefusalActResult.BUNDLE_KEY)

    private fun extractMatchingActResult(data: Intent?): MatchingActResult? =
        data?.getParcelableExtra(MatchingActResult.BUNDLE_KEY)

    private fun extractCollectActivityResponse(data: Intent?): CollectFingerprintsActResult? =
        data?.getParcelableExtra(CollectFingerprintsActResult.BUNDLE_KEY)
}

