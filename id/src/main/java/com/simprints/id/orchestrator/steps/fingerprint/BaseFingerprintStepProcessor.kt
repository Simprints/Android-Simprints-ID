package com.simprints.id.orchestrator.steps.fingerprint

import android.content.Intent
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintToDomainResponse.fromFingerprintToDomainResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintResponse
import com.simprints.id.orchestrator.steps.Step.Request
import com.simprints.id.orchestrator.steps.StepProcessor
import com.simprints.moduleapi.fingerprint.requests.IFingerprintRequest
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse
import com.simprints.moduleapi.fingerprint.requests.IFingerprintRequest.Companion.BUNDLE_KEY as REQUEST_BUNDLE_KEY
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse.Companion.BUNDLE_KEY as RESPONSE_BUNDLE_KEY

abstract class BaseFingerprintStepProcessor : StepProcessor {

    companion object {
        const val ACTIVITY_CLASS_NAME = "com.simprints.fingerprint.activities.launch.LaunchActivity"

        private const val FINGERPRINT_REQUEST_CODE = 100
        const val FINGERPRINT_ENROL_REQUEST_CODE = FINGERPRINT_REQUEST_CODE + 1
        const val FINGERPRINT_IDENTIFY_REQUEST_CODE = FINGERPRINT_REQUEST_CODE + 2
        const val FINGERPRINT_VERIFY_REQUEST_CODE = FINGERPRINT_REQUEST_CODE + 3

        fun isFingerprintResult(requestCode: Int) =
            listOf(FINGERPRINT_ENROL_REQUEST_CODE, FINGERPRINT_IDENTIFY_REQUEST_CODE, FINGERPRINT_VERIFY_REQUEST_CODE).contains(requestCode)
    }

    protected fun buildIntent(fingerprintRequest: IFingerprintRequest,
                              packageName: String): Request =
        with(Intent().setClassName(packageName, ACTIVITY_CLASS_NAME)) {
            putExtra(REQUEST_BUNDLE_KEY, fingerprintRequest)
            Request(requestCode, this)
        }

    override fun processResult(requestCode: Int, resultCode: Int, data: Intent?): FingerprintResponse? =
        data?.getParcelableExtra<IFingerprintResponse>(RESPONSE_BUNDLE_KEY)?.let {
            fromFingerprintToDomainResponse(it)
        }
}
