package com.simprints.id.orchestrator.modality

import android.content.Intent
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.event.domain.events.PersonCreationEvent
import com.simprints.id.data.db.subject.domain.FingerprintSample
import com.simprints.id.domain.moduleapi.core.requests.SetupPermission
import com.simprints.id.domain.moduleapi.face.responses.FaceExitFormResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintRefusalFormResponse
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor
import com.simprints.id.orchestrator.steps.core.requests.ConsentType
import com.simprints.id.orchestrator.steps.core.response.CoreExitFormResponse
import com.simprints.id.orchestrator.steps.core.response.CoreFaceExitFormResponse
import com.simprints.id.orchestrator.steps.core.response.CoreFingerprintExitFormResponse
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessor
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.ignoreException

abstract class ModalityFlowBaseImpl(private val coreStepProcessor: CoreStepProcessor,
                                    private val fingerprintStepProcessor: FingerprintStepProcessor,
                                    private val faceStepProcessor: FaceStepProcessor,
                                    private val timeHelper: TimeHelper,
                                    private val eventRepository: EventRepository,
                                    private val consentRequired: Boolean,
                                    private val locationRequired: Boolean) : ModalityFlow {

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

    protected fun addSetupStep() {
        steps.add(buildSetupStep())
    }

    private fun buildSetupStep() = coreStepProcessor.buildStepSetup(getPermissions())

    private fun getPermissions() = if (locationRequired) {
        listOf(SetupPermission.LOCATION)
    } else {
        emptyList()
    }
    fun completeAllStepsIfExitFormHappened(requestCode: Int, resultCode: Int, data: Intent?) =
        tryProcessingResultFromCoreStepProcessor(data)
            ?: tryProcessingResultFromFingerprintStepProcessor(requestCode, resultCode, data)
            ?: tryProcessingResultFromFaceStepProcessor(requestCode, resultCode, data)

    private fun tryProcessingResultFromCoreStepProcessor(data: Intent?) =
        coreStepProcessor.processResult(data).also { coreResult ->
            if (isExitFormResponse(coreResult)) {
                completeAllSteps()
            }
        }

    private fun isExitFormResponse(coreResult: Step.Result?) =
        coreResult is CoreExitFormResponse ||
            coreResult is CoreFingerprintExitFormResponse ||
            coreResult is CoreFaceExitFormResponse

    private fun tryProcessingResultFromFingerprintStepProcessor(requestCode: Int,
                                                                resultCode: Int,
                                                                data: Intent?) =
        fingerprintStepProcessor.processResult(requestCode, resultCode, data).also { fingerResult ->
            if (fingerResult is FingerprintRefusalFormResponse) {
                completeAllSteps()
            }
        }

    private fun tryProcessingResultFromFaceStepProcessor(requestCode: Int,
                                                                resultCode: Int,
                                                                data: Intent?) =
        faceStepProcessor.processResult(requestCode, resultCode, data).also { faceResult ->
            if (faceResult is FaceExitFormResponse) {
                completeAllSteps()
            }
        }


    private fun completeAllSteps() {
        steps.forEach { it.setStatus(Step.Status.COMPLETED) }
    }

    suspend fun extractFingerprintAndAddPersonCreationEvent(fingerprintCaptureResponse: FingerprintCaptureResponse) {
        val fingerprintSamples = extractFingerprintSamples(fingerprintCaptureResponse)
        addPersonCreationEventForFingerprintSamples(fingerprintSamples)
    }

    private fun extractFingerprintSamples(result: FingerprintCaptureResponse) =
        result.captureResult.mapNotNull { captureResult ->
            val fingerId = captureResult.identifier
            captureResult.sample?.let { sample ->
                FingerprintSample(fingerId, sample.template, sample.templateQualityScore)
            }
        }

    private suspend fun addPersonCreationEventForFingerprintSamples(fingerprintSamples: List<FingerprintSample>) {
        ignoreException {
            val currentCaptureSessionEvent = eventRepository.getCurrentCaptureSessionEvent()
            eventRepository.addEvent(PersonCreationEvent.build(timeHelper, currentCaptureSessionEvent, fingerprintSamples))
        }
    }
}
