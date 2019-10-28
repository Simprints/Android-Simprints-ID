package com.simprints.fingerprint.data.domain.moduleapi.fingerprint

import android.app.Activity
import android.content.Intent
import com.simprints.fingerprint.activities.alert.result.AlertTaskResult
import com.simprints.fingerprint.activities.collect.result.CollectFingerprintsTaskResult
import com.simprints.fingerprint.activities.matching.result.MatchingTaskResult
import com.simprints.fingerprint.activities.refusal.result.RefusalTaskResult
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintErrorReason
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintMatchResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintRefusalFormResponse
import com.simprints.fingerprint.data.domain.refusal.toFingerprintRefusalFormReason
import com.simprints.fingerprint.orchestrator.models.FinalResult
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse

class FinalResultBuilder {

    fun createCancelledResult() =
        FinalResult(Activity.RESULT_CANCELED, null)

    fun createAlertResult(alertTaskResult: AlertTaskResult) =
        FinalResult(Activity.RESULT_CANCELED, Intent().apply {
            putExtra(IFingerprintResponse.BUNDLE_KEY, DomainToFingerprintResponse.fromDomainToFingerprintErrorResponse(
                with(alertTaskResult) {
                    FingerprintErrorReason.fromFingerprintAlertToErrorResponse(alert)
                }
            ))
        })

    fun createRefusalResult(refusalTaskResult: RefusalTaskResult) =
        FinalResult(Activity.RESULT_OK, Intent().apply {
            putExtra(IFingerprintResponse.BUNDLE_KEY, DomainToFingerprintResponse.fromDomainToFingerprintRefusalFormResponse(
                with(refusalTaskResult) {
                    FingerprintRefusalFormResponse(
                        answer.reason.toFingerprintRefusalFormReason(),
                        answer.optionalText
                    )
                }
            ))
        })

    fun createCaptureResult(collectFingerprintsTaskResult: CollectFingerprintsTaskResult): FinalResult =
        FinalResult(Activity.RESULT_OK, Intent().apply {
            putExtra(IFingerprintResponse.BUNDLE_KEY, DomainToFingerprintResponse.fromDomainToFingerprintCaptureResponse(
                FingerprintCaptureResponse(collectFingerprintsTaskResult.fingerprints)
            ))
        })

    fun createMatchResult(matchingTaskResult: MatchingTaskResult) =
        FinalResult(Activity.RESULT_OK, Intent().apply {
            putExtra(IFingerprintResponse.BUNDLE_KEY, DomainToFingerprintResponse.fromDomainToFingerprintMatchResponse(
                FingerprintMatchResponse(matchingTaskResult.results)
            ))
        })
}
