package com.simprints.fingerprint.data.domain.moduleapi.fingerprint

import android.app.Activity
import android.content.Intent
import com.simprints.fingerprint.activities.alert.result.AlertTaskResult
import com.simprints.fingerprint.activities.collect.result.CollectFingerprintsTaskResult
import com.simprints.fingerprint.activities.matching.result.MatchingTaskResult
import com.simprints.fingerprint.activities.refusal.result.RefusalTaskResult
import com.simprints.fingerprint.controllers.fingerprint.config.ConfigurationTaskResult
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.DomainToFingerprintResponse.fromDomainToFingerprintCaptureResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.DomainToFingerprintResponse.fromDomainToFingerprintConfigurationResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.DomainToFingerprintResponse.fromDomainToFingerprintErrorResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.DomainToFingerprintResponse.fromDomainToFingerprintMatchResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.DomainToFingerprintResponse.fromDomainToFingerprintRefusalFormResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.*
import com.simprints.fingerprint.orchestrator.models.FinalResult
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse
import javax.inject.Inject

class FinalResultBuilder @Inject constructor(){

    fun createCancelledResult() =
        FinalResult(Activity.RESULT_CANCELED, null)

    fun createAlertResult(alertTaskResult: AlertTaskResult) =
        FinalResult(Activity.RESULT_CANCELED, Intent().apply {
            putExtra(IFingerprintResponse.BUNDLE_KEY, fromDomainToFingerprintErrorResponse(
                with(alertTaskResult) {
                    FingerprintErrorReason.fromFingerprintAlertToErrorResponse(alert)
                }
            ))
        })

    fun createRefusalResult(refusalTaskResult: RefusalTaskResult) =
        FinalResult(Activity.RESULT_OK, Intent().apply {
            putExtra(IFingerprintResponse.BUNDLE_KEY, fromDomainToFingerprintRefusalFormResponse(
                with(refusalTaskResult) {
                    FingerprintRefusalFormResponse(
                        answer.reason,
                        answer.optionalText
                    )
                }
            ))
        })

    fun createCaptureResult(collectFingerprintsTaskResult: CollectFingerprintsTaskResult): FinalResult =
        FinalResult(Activity.RESULT_OK, Intent().apply {
            putExtra(IFingerprintResponse.BUNDLE_KEY, fromDomainToFingerprintCaptureResponse(
                FingerprintCaptureResponse(collectFingerprintsTaskResult.fingerprints)
            ))
        })

    fun createMatchResult(matchingTaskResult: MatchingTaskResult) =
        FinalResult(Activity.RESULT_OK, Intent().apply {
            putExtra(IFingerprintResponse.BUNDLE_KEY, fromDomainToFingerprintMatchResponse(
                FingerprintMatchResponse(matchingTaskResult.results)
            ))
        })

    fun createConfigurationResult(@Suppress("UNUSED_PARAMETER") configurationTaskResult: ConfigurationTaskResult) =
        FinalResult(Activity.RESULT_OK, Intent().apply {
            putExtra(IFingerprintResponse.BUNDLE_KEY, fromDomainToFingerprintConfigurationResponse(
                FingerprintConfigurationResponse()
            ))
        })
}
