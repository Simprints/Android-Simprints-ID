package com.simprints.id.orchestrator.modality

import android.content.Intent
import com.simprints.feature.consent.ConsentType
import com.simprints.id.domain.moduleapi.face.responses.FaceErrorResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceExitFormResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintErrorResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintRefusalFormResponse
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor
import com.simprints.id.orchestrator.steps.core.response.ExitFormResponse
import com.simprints.id.orchestrator.steps.core.response.SetupResponse
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessor
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.GeneralConfiguration.Modality
import com.simprints.infra.authstore.AuthStore

abstract class ModalityFlowBaseImpl(
    private val coreStepProcessor: CoreStepProcessor,
    private val fingerprintStepProcessor: FingerprintStepProcessor,
    private val faceStepProcessor: FaceStepProcessor,
    private val configManager: ConfigManager,
    private val authStore: AuthStore,
    private val deviceId: String
) : ModalityFlow {

    override val steps: MutableList<Step> = mutableListOf()

    protected suspend fun addCoreConsentStepIfRequired(consentType: ConsentType) {
        val projectConfiguration = configManager.getProjectConfiguration()
        if (projectConfiguration.consent.collectConsent) {
            steps.add(buildConsentStep(consentType))
        }
    }

    private fun buildConsentStep(consentType: ConsentType) =
        coreStepProcessor.buildStepConsent(consentType)

    override fun restoreState(stepsToRestore: List<Step>) {
        steps.clear()
        steps.addAll(stepsToRestore)
    }

    protected fun addSetupStep() {
        steps.add(coreStepProcessor.buildStepSetup())
    }

    protected suspend fun addModalityConfigurationSteps() {
        val projectConfiguration = configManager.getProjectConfiguration()
        steps.addAll(buildModalityConfigurationSteps(projectConfiguration.general.modalities))
    }

    protected suspend fun addModalitiesStepsList() {
        val projectConfiguration = configManager.getProjectConfiguration()
        steps.addAll(
            projectConfiguration.general.modalities.map {
                when (it) {
                    Modality.FINGERPRINT -> fingerprintStepProcessor.buildStepToCapture()
                    Modality.FACE -> faceStepProcessor.buildCaptureStep()
                }
            }
        )
    }

    private fun buildModalityConfigurationSteps(modalities: List<Modality>) = modalities.map {
        when (it) {
            Modality.FINGERPRINT -> fingerprintStepProcessor.buildConfigurationStep()
            Modality.FACE -> faceStepProcessor.buildConfigurationStep(
                authStore.signedInProjectId,
                deviceId
            )
        }
    }

    fun completeAllStepsIfExitFormOrErrorHappened(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) =
        tryProcessingResultFromCoreStepProcessor(data)
            ?: tryProcessingResultFromFingerprintStepProcessor(requestCode, resultCode, data)
            ?: tryProcessingResultFromFaceStepProcessor(requestCode, resultCode, data)

    private fun tryProcessingResultFromCoreStepProcessor(data: Intent?) =
        coreStepProcessor.processResult(data).also { coreResult ->
            if (isExitFormResponse(coreResult) || isSetupResponseAndSetupIncomplete(coreResult)) {
                completeAllSteps()
            }
        }

    private fun isExitFormResponse(coreResult: Step.Result?) = coreResult is ExitFormResponse

    private fun isSetupResponseAndSetupIncomplete(coreResult: Step.Result?) =
        coreResult is SetupResponse && !coreResult.isSetupComplete

    private fun tryProcessingResultFromFingerprintStepProcessor(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) =
        fingerprintStepProcessor.processResult(requestCode, resultCode, data).also { fingerResult ->
            if (fingerResult is FingerprintRefusalFormResponse || fingerResult is FingerprintErrorResponse) {
                completeAllSteps()
            }
        }

    private fun tryProcessingResultFromFaceStepProcessor(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) =
        faceStepProcessor.processResult(requestCode, resultCode, data).also { faceResult ->
            if (faceResult is FaceExitFormResponse || faceResult is FaceErrorResponse) {
                completeAllSteps()
            }
        }

    private fun completeAllSteps() {
        steps.forEach { it.setStatus(Step.Status.COMPLETED) }
    }
}
