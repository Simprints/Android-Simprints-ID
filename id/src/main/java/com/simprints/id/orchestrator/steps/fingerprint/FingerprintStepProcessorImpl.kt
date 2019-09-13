package com.simprints.id.orchestrator.steps.fingerprint

import android.content.Intent
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactory
import com.simprints.id.domain.moduleapi.fingerprint.ModuleApiToDomainFingerprintResponse
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintResponse
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.NOT_STARTED
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintRequestCode.*
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintRequestCode.Companion.isFingerprintResult
import com.simprints.moduleapi.fingerprint.requests.IFingerprintRequest
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse.Companion.BUNDLE_KEY as RESPONSE_BUNDLE_KEY

class FingerprintStepProcessorImpl(private val fingerprintRequestFactory: FingerprintRequestFactory,
                                   private val converterModuleApiToDomain: ModuleApiToDomainFingerprintResponse,
                                   private val prefs: PreferencesManager) : FingerprintStepProcessor {

    companion object {
        const val ACTIVITY_CLASS_NAME = "com.simprints.fingerprint.activities.orchestrator.OrchestratorActivity"
    }

    override fun buildStepEnrol(projectId: String,
                                userId: String,
                                moduleId: String,
                                metadata: String): Step =
        fingerprintRequestFactory.buildFingerprintEnrolRequest(projectId, userId, moduleId, metadata, prefs).run {
            buildStep(ENROL, this)
        }

    override fun buildStepIdentify(projectId: String,
                                   userId: String,
                                   moduleId: String,
                                   metadata: String): Step =
        fingerprintRequestFactory.buildFingerprintIdentifyRequest(projectId, userId, moduleId, metadata, prefs).run {
            buildStep(IDENTIFY, this)
        }

    override fun buildStepVerify(projectId: String,
                                 userId: String,
                                 moduleId: String,
                                 metadata: String,
                                 verifyGuid: String): Step =
        fingerprintRequestFactory.buildFingerprintVerifyRequest(projectId, userId, moduleId, metadata, verifyGuid, prefs).run {
            buildStep(VERIFY, this)
        }

    private fun buildStep(requestCode: FingerprintRequestCode, request: FingerprintRequest): Step =
        Step(requestCode.value, ACTIVITY_CLASS_NAME, IFingerprintRequest.BUNDLE_KEY, request, status = NOT_STARTED)

    override fun processResult(requestCode: Int, resultCode: Int, data: Intent?): FingerprintResponse? =
        if (isFingerprintResult(requestCode)) {
            data?.getParcelableExtra<IFingerprintResponse>(RESPONSE_BUNDLE_KEY)?.let {
                converterModuleApiToDomain.fromModuleApiToDomainFingerprintResponse(it)
            }
        } else {
            null
        }
}
