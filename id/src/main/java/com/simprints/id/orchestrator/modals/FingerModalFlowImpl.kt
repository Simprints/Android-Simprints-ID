package com.simprints.id.orchestrator.modals

import android.app.Activity
import android.content.Intent
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.modal.ModalResponse
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.fingerprint.DomainToFingerprintRequest.fromDomainToFingerprintRequest
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactory.buildFingerprintRequest
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintToDomainResponse.fromFingerprintToDomainResponse
import com.simprints.id.orchestrator.ModalStepRequest
import com.simprints.id.orchestrator.modals.ModalFlowIntentRequestCodes.REQUEST_CODE_FINGERPRINT
import com.simprints.moduleapi.fingerprint.requests.IFingerprintRequest
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse
import io.reactivex.Observable
import io.reactivex.ObservableEmitter

class FingerModalFlowImpl(private val packageName: String,
                          private val appRequest: AppRequest,
                          private val prefs: PreferencesManager) : ModalFlow {

    companion object {
        const val launchFingerprintClassName = "com.simprints.fingerprint.activities.launch.LaunchActivity"
    }

    private lateinit var responsesEmitter: ObservableEmitter<ModalResponse>
    override var modalResponses: Observable<ModalResponse> = Observable.create {
        responsesEmitter = it
    }

    private lateinit var nextIntentEmitter: ObservableEmitter<ModalStepRequest>
    override var nextIntent: Observable<ModalStepRequest> = Observable.create {
        nextIntentEmitter = it
        nextIntentEmitter.onNext(getNextIntent())
    }

    private fun getNextIntent(): ModalStepRequest {
        val intent = Intent().setClassName(packageName, launchFingerprintClassName)
        intent.putExtra(IFingerprintRequest.BUNDLE_KEY, fromDomainToFingerprintRequest(buildFingerprintRequest(appRequest, prefs)))
        return ModalStepRequest(REQUEST_CODE_FINGERPRINT, intent)
    }

    @Throws(IllegalArgumentException::class)
    override fun handleIntentResponse(requestCode: Int, resultCode: Int, data: Intent?) {
        return try {
            val potentialModalResponse = extractModalResponse(data)
            require(resultCode == Activity.RESULT_OK)
            require(potentialModalResponse != null)

            responsesEmitter.onNext(potentialModalResponse)
            responsesEmitter.onComplete()
            nextIntentEmitter.onComplete()
        } catch (t: Throwable) {
            t.printStackTrace()
            responsesEmitter.onError(t)
            nextIntentEmitter.onError(t)
        }
    }

    private fun extractModalResponse(data: Intent?): ModalResponse? {
        val potentialFingerprintResponse = data?.getParcelableExtra<IFingerprintResponse>(IFingerprintResponse.BUNDLE_KEY)
        require(potentialFingerprintResponse != null)

        return fromFingerprintToDomainResponse(potentialFingerprintResponse)
    }
}
