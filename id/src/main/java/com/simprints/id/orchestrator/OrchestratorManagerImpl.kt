package com.simprints.id.orchestrator

import android.app.Activity
import android.content.Intent
import com.simprints.face.data.moduleapi.face.responses.FaceIdentifyResponse
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.modal.Modal
import com.simprints.id.domain.modal.Modal.*
import com.simprints.id.domain.modal.ModalResponse
import com.simprints.id.domain.moduleapi.app.AppResponseFactory.buildAppEnrolResponse
import com.simprints.id.domain.moduleapi.app.AppResponseFactory.buildAppIdentifyResponse
import com.simprints.id.domain.moduleapi.app.AppResponseFactory.buildAppVerifyResponse
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.face.FaceRequestFactory.buildFaceRequest
import com.simprints.id.domain.moduleapi.face.FaceToDomainResponse.fromFaceToDomainResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceEnrolResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceVerifyResponse
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactory.buildFingerprintRequest
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintToDomainResponse.fromFingerprintToDomainResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintEnrolResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintIdentifyResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintVerifyResponse
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceResponse
import com.simprints.moduleapi.fingerprint.requests.IFingerprintRequest
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse
import io.reactivex.subjects.BehaviorSubject

class OrchestratorManagerImpl(private val modal: Modal,
                              private val prefs: PreferencesManager) : OrchestratorManager {

    companion object {
        const val packageName = "com.simprints.id"
        const val launchFingerprintClassName = "com.simprints.fingerprint.activities.launch.LaunchActivity"
        const val launchFaceClassName = "com.simprints.face.activities.FaceCaptureActivity"

        const val REQUEST_CODE_FACE = 1
        const val REQUEST_CODE_FINGER = 2
    }

    override var finalAppResponse: AppResponse? = null
    override val flow = BehaviorSubject.create<ModalStep>()
    private lateinit var appRequest: AppRequest
    private lateinit var sessionId: String
    private val stepResponses: MutableList<ModalResponse> = mutableListOf()

    override fun startFlow(appRequest: AppRequest, sessionId: String) {
        this.appRequest = appRequest
        this.sessionId = sessionId

        val intent =
            when (modal) {
                FACE, FACE_FINGER ->
                    ModalStep(REQUEST_CODE_FACE, createIntentForFace(appRequest))
                FINGER, FINGER_FACE ->
                    ModalStep(REQUEST_CODE_FINGER, createIntentForFinger(appRequest))
            }

        flow.onNext(intent)
    }

    private fun createIntentForFinger(appRequest: AppRequest): Intent =
        Intent().setClassName(packageName, launchFaceClassName)
            .also {
                it.putExtra(IFingerprintRequest.BUNDLE_KEY, buildFingerprintRequest(appRequest, prefs))
            }

    private fun createIntentForFace(appRequest: AppRequest): Intent =
        Intent().setClassName(packageName, launchFingerprintClassName)
            .also { it.putExtra(IFaceRequest.BUNDLE_KEY, buildFaceRequest(appRequest)) }

    override fun notifyResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val potentialModalResponse = extractModalResponse(data)
        if (resultCode == Activity.RESULT_OK && potentialModalResponse != null) {
            stepResponses.add(potentialModalResponse)
            when (modal) {
                FACE_FINGER ->
                    if (requestCode == REQUEST_CODE_FACE) {
                        createIntentForFinger(appRequest)
                    } else {
                        buildFinalAppResponseForFaceFingerprint(
                            potentialModalResponse as FaceResponse,
                            stepResponses.filterIsInstance(FingerprintResponse::class.java).first())
                    }
                FINGER_FACE ->
                    if (requestCode == REQUEST_CODE_FINGER) {
                        createIntentForFace(appRequest)
                    } else {
                        buildFinalAppResponseForFingerprintFace(
                            potentialModalResponse as FingerprintResponse,
                            stepResponses.filterIsInstance(FaceResponse::class.java).first())
                    }
                FACE -> finalAppResponse = buildFinalAppResponseForFace(potentialModalResponse as FaceResponse)
                FINGER -> buildFinalAppResponseForFingerprint(potentialModalResponse as FingerprintResponse)
            }
        } else {
            flow.onError(Throwable("Step failed")) //StopShip: Error
        }
    }

    private fun buildFinalAppResponseForFaceFingerprint(faceResponse: FaceResponse, fingerprintResponse: FingerprintResponse) {
        buildFinalSameAppResponseForFaceFingerprint(faceResponse, fingerprintResponse) //For now I do the same for face_fingerprint or fingerprint_face
    }

    private fun buildFinalAppResponseForFingerprintFace(fingerprintResponse: FingerprintResponse, faceResponse: FaceResponse) {
        buildFinalSameAppResponseForFaceFingerprint(faceResponse, fingerprintResponse) //For now I do the same for face_fingerprint or fingerprint_face
    }

    private fun buildFinalSameAppResponseForFaceFingerprint(faceResponse: FaceResponse, fingerprintResponse: FingerprintResponse) {
        when (appRequest) {
            is AppEnrolRequest -> buildAppEnrolResponse(fingerprintResponse as FingerprintEnrolResponse, faceResponse as FaceEnrolResponse)
            is AppIdentifyRequest -> buildAppIdentifyResponse(fingerprintResponse as FingerprintIdentifyResponse, faceResponse as FaceIdentifyResponse, sessionId)
            is AppVerifyRequest -> buildAppVerifyResponse(fingerprintResponse as FingerprintVerifyResponse, faceResponse as FaceVerifyResponse)
            else -> null
        }
    }

    private fun extractModalResponse(data: Intent?): ModalResponse? {
        val potentialFingerprintResponse = data?.getParcelableExtra<IFingerprintResponse>(IFingerprintResponse.BUNDLE_KEY)
        if (potentialFingerprintResponse != null) {
            return fromFingerprintToDomainResponse(potentialFingerprintResponse)
        }

        val potentialFaceResponse = data?.getParcelableExtra<IFaceResponse>(IFaceResponse.BUNDLE_KEY)
        if (potentialFaceResponse != null) {
            return fromFaceToDomainResponse(potentialFaceResponse)
        }

        return null
    }

    private fun buildFinalAppResponseForFace(faceResponse: FaceResponse): AppResponse? =
        when (appRequest) {
            is AppEnrolRequest -> buildAppEnrolResponse(faceResponse as FaceEnrolResponse)
            is AppIdentifyRequest -> buildAppIdentifyResponse(faceResponse as FaceIdentifyResponse, sessionId)
            is AppVerifyRequest -> buildAppVerifyResponse(faceResponse as FaceVerifyResponse)
            else -> null
        }

    private fun buildFinalAppResponseForFingerprint(faceResponse: FingerprintResponse): AppResponse? =
        when (appRequest) {
            is AppEnrolRequest -> buildAppEnrolResponse(faceResponse as FingerprintEnrolResponse)
            is AppIdentifyRequest -> buildAppIdentifyResponse(faceResponse as FingerprintIdentifyResponse, sessionId)
            is AppVerifyRequest -> buildAppVerifyResponse(faceResponse as FingerprintVerifyResponse)
            else -> null
        }

}
