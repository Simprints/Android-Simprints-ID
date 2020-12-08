package com.simprints.id.orchestrator.modality

import android.content.Intent
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.face.responses.FaceErrorResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceExitFormResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintErrorResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintRefusalFormResponse
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor
import com.simprints.id.orchestrator.steps.core.requests.ConsentType
import com.simprints.id.orchestrator.steps.core.requests.SetupPermission
import com.simprints.id.orchestrator.steps.core.response.CoreExitFormResponse
import com.simprints.id.orchestrator.steps.core.response.CoreFaceExitFormResponse
import com.simprints.id.orchestrator.steps.core.response.CoreFingerprintExitFormResponse
import com.simprints.id.orchestrator.steps.core.response.SetupResponse
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessor
import com.simprints.id.tools.time.TimeHelper

abstract class ModalityFlowBaseImpl(private val coreStepProcessor: CoreStepProcessor,
                                    private val fingerprintStepProcessor: FingerprintStepProcessor,
                                    private val faceStepProcessor: FaceStepProcessor,
                                    private val timeHelper: TimeHelper,
                                    private val eventRepository: EventRepository,
                                    private val consentRequired: Boolean,
                                    private val locationRequired: Boolean,
                                    private val modalities: List<Modality>,
                                    private val projectId: String,
                                    private val deviceId: String) : ModalityFlow {

    override val steps: MutableList<Step> = mutableListOf()

    protected fun addCoreConsentStepIfRequired(consentType: ConsentType) {
        if (consentRequired) {
            steps.add(buildConsentStep(consentType))
        }
    }

    private fun buildConsentStep(consentType: ConsentType) =
        coreStepProcessor.buildStepConsent(consentType)

    override fun restoreState(stepsToRestore: List<Step>) {
        steps.clear()
        steps.addAll(stepsToRestore)
    }

    protected fun addModalityConfigurationSteps(modalities: List<Modality>) {
        steps.addAll(buildModalityConfigurationSteps(modalities))
    }

    protected fun addSetupStep() {
        steps.add(buildSetupStep())
    }

    private fun buildModalityConfigurationSteps(modalities: List<Modality>) = modalities.map {
        when (it) {
            Modality.FINGER -> fingerprintStepProcessor.buildConfigurationStep()
            Modality.FACE -> faceStepProcessor.buildConfigurationStep(projectId, deviceId)
        }
    }

    private fun buildSetupStep() = coreStepProcessor.buildStepSetup(modalities, getPermissions())

    private fun getPermissions() = if (locationRequired) {
        listOf(SetupPermission.LOCATION)
    } else {
        emptyList()
    }

    fun completeAllStepsIfExitFormOrErrorHappened(requestCode: Int, resultCode: Int, data: Intent?) =
        tryProcessingResultFromCoreStepProcessor(data)
            ?: tryProcessingResultFromFingerprintStepProcessor(requestCode, resultCode, data)
            ?: tryProcessingResultFromFaceStepProcessor(requestCode, resultCode, data)

    private fun tryProcessingResultFromCoreStepProcessor(data: Intent?) =
        coreStepProcessor.processResult(data).also { coreResult ->
            if (isExitFormResponse(coreResult) || isSetupResponseAndSetupIncomplete(coreResult)) {
                completeAllSteps()
            }
        }

    private fun isExitFormResponse(coreResult: Step.Result?) =
        coreResult is CoreExitFormResponse ||
            coreResult is CoreFingerprintExitFormResponse ||
            coreResult is CoreFaceExitFormResponse

    private fun isSetupResponseAndSetupIncomplete(coreResult: Step.Result?) =
        coreResult is SetupResponse && !coreResult.isSetupComplete

    private fun tryProcessingResultFromFingerprintStepProcessor(requestCode: Int,
                                                                resultCode: Int,
                                                                data: Intent?) =
        fingerprintStepProcessor.processResult(requestCode, resultCode, data).also { fingerResult ->
            if (fingerResult is FingerprintRefusalFormResponse || fingerResult is FingerprintErrorResponse) {
                completeAllSteps()
            }
        }

    private fun tryProcessingResultFromFaceStepProcessor(requestCode: Int,
                                                         resultCode: Int,
                                                         data: Intent?) =
        faceStepProcessor.processResult(requestCode, resultCode, data).also { faceResult ->
            if (faceResult is FaceExitFormResponse || faceResult is FaceErrorResponse) {
                completeAllSteps()
            }
        }

    private fun completeAllSteps() {
        steps.forEach { it.setStatus(Step.Status.COMPLETED) }
    }
}
