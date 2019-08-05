package com.simprints.id.orchestrator.modality.flows

import android.app.Activity
import android.content.Intent
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.fingerprint.DomainToFingerprintRequest.fromDomainToFingerprintRequest
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactory.buildFingerprintRequest
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintToDomainResponse.fromFingerprintToDomainResponse
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow.Request
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow.Step
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow.Step.Status.ONGOING
import com.simprints.id.orchestrator.modality.flows.interfaces.SingleModalityFlow
import com.simprints.moduleapi.fingerprint.requests.IFingerprintRequest.Companion.BUNDLE_KEY
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse

class FingerprintModalityFlow(private val appRequest: AppRequest,
                              private val packageName: String,
                              private val prefs: PreferencesManager) : SingleModalityFlow {

    companion object {
        const val fingerprintActivityClassName = "com.simprints.fingerprint.activities.launch.LaunchActivity"
        const val REQUEST_CODE_FINGERPRINT = 2
    }

    override val steps = listOf(Step(getModalityStepRequestForFace(), ONGOING))

    override fun getLatestOngoingStep(): Step? = steps.firstOrNull { it.status == ONGOING }

    private fun getModalityStepRequestForFace(): Request {
        val intent = Intent().setClassName(packageName, fingerprintActivityClassName)
        val domainFingerprintRequest = buildFingerprintRequest(appRequest, prefs)
        intent.putExtra(BUNDLE_KEY, fromDomainToFingerprintRequest(domainFingerprintRequest))
        return Request(REQUEST_CODE_FINGERPRINT, intent)
    }

    @Throws(IllegalArgumentException::class)
    override fun handleIntentResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean =
        if (isFaceResult(requestCode)) {
            require(resultCode == Activity.RESULT_OK && data != null)

            val potentialFaceResponse = data.getParcelableExtra<IFingerprintResponse>(BUNDLE_KEY)
            fromFingerprintToDomainResponse(potentialFaceResponse).also {
                steps[1].result = it
            }
            true
        } else {
            false
        }

    private fun isFaceResult(requestCode: Int): Boolean = requestCode == REQUEST_CODE_FINGERPRINT
}
