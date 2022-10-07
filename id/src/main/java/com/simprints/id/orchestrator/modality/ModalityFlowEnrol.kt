package com.simprints.id.orchestrator.modality

import android.content.Intent
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.AppEnrolRequest
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureSample
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureSample
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.NOT_STARTED
import com.simprints.id.orchestrator.steps.core.CoreRequestCode.Companion.isCoreResult
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor
import com.simprints.id.orchestrator.steps.core.requests.ConsentType.ENROL
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.Companion.isFaceResult
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintRequestCode.Companion.isFingerprintResult
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessor
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.IdentificationConfiguration.PoolType
import com.simprints.infra.enrolment.records.domain.models.SubjectQuery
import com.simprints.infra.login.LoginManager

class ModalityFlowEnrol(
    private val fingerprintStepProcessor: FingerprintStepProcessor,
    private val faceStepProcessor: FaceStepProcessor,
    private val coreStepProcessor: CoreStepProcessor,
    private val configManager: ConfigManager,
    loginManager: LoginManager,
    deviceId: String,
) :
    ModalityFlowBaseImpl(
        coreStepProcessor,
        fingerprintStepProcessor,
        faceStepProcessor,
        configManager,
        loginManager,
        deviceId
    ) {

    override suspend fun startFlow(appRequest: AppRequest) {
        require(appRequest is AppEnrolRequest)
        addSetupStep()
        addModalityConfigurationSteps()
        addCoreConsentStepIfRequired(ENROL)
        addModalitiesStepsList()
    }

    override fun getNextStepToLaunch(): Step? = steps.firstOrNull { it.getStatus() == NOT_STARTED }

    override suspend fun handleIntentResult(
        appRequest: AppRequest,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ): Step? {
        require(appRequest is AppEnrolRequest)
        val projectConfiguration = configManager.getProjectConfiguration()
        val result = when {
            isCoreResult(requestCode) -> coreStepProcessor.processResult(data)
            isFingerprintResult(requestCode) -> {
                fingerprintStepProcessor.processResult(requestCode, resultCode, data)
            }
            isFaceResult(requestCode) -> faceStepProcessor.processResult(
                requestCode,
                resultCode,
                data
            )
            else -> throw IllegalStateException("Invalid result from intent")
        }
        completeAllStepsIfExitFormOrErrorHappened(requestCode, resultCode, data)

        val stepForRequest = steps.firstOrNull { it.requestCode == requestCode }
        return stepForRequest?.apply { setResult(result) }.also {
            if (projectConfiguration.general.duplicateBiometricEnrolmentCheck) {
                with(appRequest) {
                    buildQueryAndAddMatchingStepIfRequired(result, projectId, userId, moduleId)
                }
            }
        }
    }

    private suspend fun buildQueryAndAddMatchingStepIfRequired(
        result: Step.Result?,
        projectId: String,
        userId: String,
        moduleId: String
    ) {
        val projectConfiguration = configManager.getProjectConfiguration()
        if (result is FingerprintCaptureResponse) {
            val query = buildQuery(
                projectId,
                userId,
                moduleId,
                projectConfiguration.identification.poolType
            )
            addMatchingStepForFinger(result.captureResult.mapNotNull { it.sample }, query)
        } else if (result is FaceCaptureResponse) {
            val query = buildQuery(
                projectId,
                userId,
                moduleId,
                projectConfiguration.identification.poolType
            )
            addMatchingStepForFace(result.capturingResult.mapNotNull { it.result }, query)
        }
    }

    private fun buildQuery(
        projectId: String,
        userId: String,
        moduleId: String,
        matchGroup: PoolType
    ): SubjectQuery =
        when (matchGroup) {
            PoolType.PROJECT -> SubjectQuery(projectId)
            PoolType.USER -> SubjectQuery(projectId, attendantId = userId)
            PoolType.MODULE -> SubjectQuery(projectId, moduleId = moduleId)
        }

    private fun addMatchingStepForFinger(
        probeSamples: List<FingerprintCaptureSample>,
        query: SubjectQuery
    ) {
        steps.add(fingerprintStepProcessor.buildStepToMatch(probeSamples, query))
    }

    private fun addMatchingStepForFace(probeSamples: List<FaceCaptureSample>, query: SubjectQuery) {
        steps.add(faceStepProcessor.buildStepMatch(probeSamples, query))
    }
}
