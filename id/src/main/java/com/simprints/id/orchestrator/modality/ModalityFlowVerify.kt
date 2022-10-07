package com.simprints.id.orchestrator.modality

import android.content.Intent
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.AppVerifyRequest
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureSample
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureSample
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.NOT_STARTED
import com.simprints.id.orchestrator.steps.core.CoreRequestCode.Companion.isCoreResult
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor
import com.simprints.id.orchestrator.steps.core.requests.ConsentType.VERIFY
import com.simprints.id.orchestrator.steps.core.response.FetchGUIDResponse
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.Companion.isFaceResult
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintRequestCode.Companion.isFingerprintResult
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessor
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.enrolment.records.domain.models.SubjectQuery
import com.simprints.infra.login.LoginManager

class ModalityFlowVerify(
    private val fingerprintStepProcessor: FingerprintStepProcessor,
    private val faceStepProcessor: FaceStepProcessor,
    private val coreStepProcessor: CoreStepProcessor,
    configManager: ConfigManager,
    loginManager: LoginManager,
    deviceId: String
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
        require(appRequest is AppVerifyRequest)
        addSetupStep()
        addModalityConfigurationSteps()
        addCoreFetchGuidStep(appRequest.projectId, appRequest.verifyGuid)
        addCoreConsentStepIfRequired(VERIFY)
        addModalitiesStepsList()
    }

    private fun addCoreFetchGuidStep(projectId: String, verifyGuid: String) =
        steps.add(coreStepProcessor.buildFetchGuidStep(projectId, verifyGuid))

    override fun getNextStepToLaunch(): Step? = steps.firstOrNull { it.getStatus() == NOT_STARTED }

    override suspend fun handleIntentResult(
        appRequest: AppRequest,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ): Step? {
        require(appRequest is AppVerifyRequest)

        val result = when {
            isCoreResult(requestCode) -> {
                coreStepProcessor.processResult(data).also {
                    completeAllStepsIfFetchGuidResponseAndFailed(it)
                }
            }
            isFingerprintResult(requestCode) -> fingerprintStepProcessor.processResult(
                requestCode,
                resultCode,
                data
            )
            isFaceResult(requestCode) -> faceStepProcessor.processResult(
                requestCode,
                resultCode,
                data
            )
            else -> throw IllegalStateException("Invalid result from intent")
        }

        completeAllStepsIfExitFormOrErrorHappened(requestCode, resultCode, data)

        val stepRequested = steps.firstOrNull { it.requestCode == requestCode }
        stepRequested?.setResult(result)

        return stepRequested.also {
            buildQueryAndAddMatchingStepIfRequired(result, appRequest)
        }
    }

    private fun buildQueryAndAddMatchingStepIfRequired(
        result: Step.Result?,
        appRequest: AppVerifyRequest
    ) {
        if (result is FingerprintCaptureResponse) {
            val query = SubjectQuery(subjectId = appRequest.verifyGuid)
            addMatchingStep(result.captureResult.mapNotNull { it.sample }, query)
        } else if (result is FaceCaptureResponse) {
            val query = SubjectQuery(subjectId = appRequest.verifyGuid)
            addMatchingStepForFace(result.capturingResult.mapNotNull { it.result }, query)
        }
    }

    private fun addMatchingStep(probeSamples: List<FingerprintCaptureSample>, query: SubjectQuery) {
        steps.add(fingerprintStepProcessor.buildStepToMatch(probeSamples, query))
    }

    private fun addMatchingStepForFace(probeSamples: List<FaceCaptureSample>, query: SubjectQuery) {
        steps.add(faceStepProcessor.buildStepMatch(probeSamples, query))
    }

    private fun completeAllStepsIfFetchGuidResponseAndFailed(result: Step.Result?) {
        if (result is FetchGUIDResponse && !result.isGuidFound) {
            steps.forEach { it.setStatus(Step.Status.COMPLETED) }
        }
    }
}
