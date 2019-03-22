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
import com.simprints.moduleapi.fingerprint.requests.IFingerprintRequest
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse
import io.reactivex.Observable
import io.reactivex.ObservableEmitter

class ModalFlowFingerprintImpl(private val packageName: String,
                               private val appRequest: AppRequest,
                               private val prefs: PreferencesManager) : ModalFlow {

    companion object {
        const val launchFingerprintClassName = "com.simprints.fingerprint.activities.launch.LaunchActivity"
        const val REQUEST_CODE_FINGERPRINT = 1
    }

    private lateinit var emitter: ObservableEmitter<ModalStepRequest>
    override var flow: Observable<ModalStepRequest> = Observable.create { emitter = it }

    override fun startFlow() {
        emitter.onNext(getNextIntent())
    }

    private fun getNextIntent(): ModalStepRequest {
        val intent = Intent().setClassName(packageName, launchFingerprintClassName)
        intent.putExtra(IFingerprintRequest.BUNDLE_KEY, fromDomainToFingerprintRequest(buildFingerprintRequest(appRequest, prefs)))
        return ModalStepRequest(REQUEST_CODE_FINGERPRINT, intent)
    }

    @Throws(IllegalArgumentException::class)
    override fun handleModalResponse(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            val potentialModalResponse = extractModalResponse(data)
            require(resultCode == Activity.RESULT_OK)
            require(potentialModalResponse != null)

            emitter.onComplete()
        } catch (t: Throwable) {
            t.printStackTrace()
            emitter.onError(t)
        }
    }

    private fun extractModalResponse(data: Intent?): ModalResponse? {
        val potentialFingerprintResponse = data?.getParcelableExtra<IFingerprintResponse>(IFingerprintResponse.BUNDLE_KEY)
        require(potentialFingerprintResponse != null)

        return fromFingerprintToDomainResponse(potentialFingerprintResponse)
    }
}
