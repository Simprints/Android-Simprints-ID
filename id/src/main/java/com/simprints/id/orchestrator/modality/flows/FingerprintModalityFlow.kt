package com.simprints.id.orchestrator.modality.flows

import android.app.Activity
import android.content.Intent
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.modality.ModalityResponse
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.fingerprint.DomainToFingerprintRequest.fromDomainToFingerprintRequest
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactory.buildFingerprintRequest
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintToDomainResponse.fromFingerprintToDomainResponse
import com.simprints.id.orchestrator.modality.ModalityStepRequest
import com.simprints.moduleapi.fingerprint.requests.IFingerprintRequest
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse

class FingerprintModalityFlow(private val appRequest: AppRequest,
                              private val packageName: String,
                              private val prefs: PreferencesManager) : SingleModalityFlowBase() {

    companion object {
        const val fingerprintActivityClassName = "com.simprints.fingerprint.activities.orchestrator.Orchestrator"
        const val REQUEST_CODE_FINGERPRINT = 2
    }

    override val intentRequestCode: Int = REQUEST_CODE_FINGERPRINT

    override fun getModalityStepRequests(): ModalityStepRequest {
        val intent = Intent().setClassName(packageName, fingerprintActivityClassName)
        intent.putExtra(IFingerprintRequest.BUNDLE_KEY, fromDomainToFingerprintRequest(buildFingerprintRequest(appRequest, prefs)))
        return ModalityStepRequest(intentRequestCode, intent)
    }

    override fun extractModalityResponse(requestCode: Int, resultCode: Int, data: Intent?): ModalityResponse {
        require(resultCode == Activity.RESULT_OK)
        require(data != null)

        val potentialFingerprintResponse = data.getParcelableExtra<IFingerprintResponse>(IFingerprintResponse.BUNDLE_KEY)
        return fromFingerprintToDomainResponse(potentialFingerprintResponse)
    }
}
