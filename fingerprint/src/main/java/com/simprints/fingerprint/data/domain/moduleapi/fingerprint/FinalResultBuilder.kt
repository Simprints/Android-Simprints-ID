package com.simprints.fingerprint.data.domain.moduleapi.fingerprint

import android.app.Activity
import android.content.Intent
import com.simprints.fingerprint.activities.alert.result.AlertTaskResult
import com.simprints.fingerprint.activities.collect.result.CollectFingerprintsTaskResult
import com.simprints.fingerprint.activities.matching.result.MatchingTaskIdentifyResult
import com.simprints.fingerprint.activities.matching.result.MatchingTaskVerifyResult
import com.simprints.fingerprint.activities.refusal.result.RefusalTaskResult
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.*
import com.simprints.fingerprint.data.domain.refusal.toFingerprintRefusalFormReason
import com.simprints.fingerprint.orchestrator.taskflow.FinalResult
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

    fun createEnrolResult(collectFingerprintsTaskResult: CollectFingerprintsTaskResult) =
        FinalResult(Activity.RESULT_OK, Intent().apply {
            putExtra(IFingerprintResponse.BUNDLE_KEY, DomainToFingerprintResponse.fromDomainToFingerprintEnrolResponse(
                FingerprintEnrolResponse(collectFingerprintsTaskResult.probe.patientId))
            )
        })

    fun createIdentifyResult(matchingTaskIdentifyResult: MatchingTaskIdentifyResult) =
        FinalResult(Activity.RESULT_OK, Intent().apply {
            putExtra(IFingerprintResponse.BUNDLE_KEY, DomainToFingerprintResponse.fromDomainToFingerprintIdentifyResponse(
                FingerprintIdentifyResponse(matchingTaskIdentifyResult.identifications)
            ))
        })

    fun createVerifyResult(matchingTaskVerifyResult: MatchingTaskVerifyResult) =
        FinalResult(Activity.RESULT_OK, Intent().apply {
            putExtra(IFingerprintResponse.BUNDLE_KEY, DomainToFingerprintResponse.fromDomainToFingerprintVerifyResponse(
                with(matchingTaskVerifyResult) {
                    FingerprintVerifyResponse(guid, confidence, tier)
                }
            ))
        })
}
