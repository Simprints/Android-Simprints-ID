package com.simprints.id.orchestrator.modality.flows

import android.app.Activity
import android.content.Intent
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.face.FaceToDomainResponse
import com.simprints.id.domain.moduleapi.fingerprint.DomainToFingerprintRequest.fromDomainToFingerprintRequest
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactory.buildFingerprintRequest
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintToDomainResponse.fromFingerprintToDomainResponse
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow
import com.simprints.id.orchestrator.modality.flows.interfaces.SingleModalityFlow
import com.simprints.moduleapi.face.responses.IFaceResponse
import com.simprints.moduleapi.fingerprint.requests.IFingerprintRequest
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse

class FingerprintModalityFlow(private val appRequest: AppRequest,
                              private val packageName: String,
                              private val prefs: PreferencesManager) : SingleModalityFlow {

    companion object {
        const val fingerprintActivityClassName = "com.simprints.fingerprint.activities.launch.LaunchActivity"
        const val REQUEST_CODE_FINGERPRINT = 2
    }

    override val steps = linkedMapOf<Int, ModalityFlow.Step?>(REQUEST_CODE_FINGERPRINT to null)

    override val nextRequest: ModalityFlow.Request?
        get() {
            val stepNotStartedYet =
                steps.entries.find { it.value == null || it.value?.response == null } // First step not started or waiting for response
            (return if(stepNotStartedYet != null) {

                // Already build the request - waiting for the response
                stepNotStartedYet.value?.request ?: run {

                    // Never built the request yet
                    val requestCode = stepNotStartedYet.key
                    getModalityStepRequests(requestCode).also {
                        steps[requestCode] = ModalityFlow.Step(it)
                    }
                }
            } else {
                null
            })
        }

    private fun getModalityStepRequests(requestCode: Int): ModalityFlow.Request =
        if (requestCode == REQUEST_CODE_FINGERPRINT) {
            getModalityStepRequestForFingerprint()
        } else {
            throw IllegalArgumentException("") //StopShip: Handle it
        }


    private fun getModalityStepRequestForFingerprint(): ModalityFlow.Request {
        val intent = Intent().setClassName(packageName, fingerprintActivityClassName)
        intent.putExtra(IFingerprintRequest.BUNDLE_KEY,
            fromDomainToFingerprintRequest(buildFingerprintRequest(appRequest, prefs)))
        return ModalityFlow.Request(REQUEST_CODE_FINGERPRINT, intent)
    }

    override fun handleIntentResult(requestCode: Int, resultCode: Int, data: Intent?): ModalityFlow.Response? =
        try {
            require(resultCode == Activity.RESULT_OK)
            require(requestCode == REQUEST_CODE_FINGERPRINT)
            require(data != null)

            val potentialFingerprintResponse = data.getParcelableExtra<IFingerprintResponse>(IFingerprintResponse.BUNDLE_KEY)
            fromFingerprintToDomainResponse(potentialFingerprintResponse).also {
                steps[requestCode]?.response = it
            }
        } catch (t: Throwable) {
            if(t !is IllegalArgumentException) {
                t.printStackTrace()
            }
            null
        }
}
