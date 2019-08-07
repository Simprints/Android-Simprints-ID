package com.simprints.id.orchestrator.modality.steps.fingerprint

import android.content.Intent
import com.simprints.id.orchestrator.modality.steps.Step.Request
import com.simprints.id.orchestrator.modality.steps.StepProcessor
import com.simprints.moduleapi.fingerprint.requests.IFingerprintRequest
import com.simprints.moduleapi.fingerprint.requests.IFingerprintRequest.Companion.BUNDLE_KEY

abstract class BaseFingerprintStepProcessor : StepProcessor {

    companion object {
        const val FINGERPRINT_REQUEST_CODE = 100
        private const val ACTIVITY_CLASS_NAME = "com.simprints.fingerprint.activities.launch.LaunchActivity"
    }

    protected fun buildIntent(fingerprintRequest: IFingerprintRequest,
                              packageName: String): Request {
        val intent = Intent().setClassName(packageName, ACTIVITY_CLASS_NAME)
        intent.putExtra(BUNDLE_KEY, fingerprintRequest)
        return Request(requestCode, intent)
    }
}
