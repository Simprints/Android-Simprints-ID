package com.simprints.id.orchestrator.modality

import android.content.Intent
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.db.session.domain.models.events.PersonCreationEvent
import com.simprints.id.data.db.subject.domain.FaceSample
import com.simprints.id.data.db.subject.domain.FingerprintSample
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.core.requests.SetupPermission
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
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
                                    private val sessionRepository: SessionRepository,
                                    private val consentRequired: Boolean,
                                    private val locationRequired: Boolean,
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

    suspend fun extractFaceAndAddPersonCreationEvent(faceCaptureResponse: FaceCaptureResponse) {
        val faceSamples = extractFaceSamples(faceCaptureResponse)
        addPersonCreationEventForFaceSamples(faceSamples)
    }

    private fun extractFingerprintSamples(result: FingerprintCaptureResponse) =
        result.captureResult.mapNotNull { captureResult ->
            val fingerId = captureResult.identifier
            captureResult.sample?.let { sample ->
                FingerprintSample(fingerId, sample.template, sample.templateQualityScore)
            }
        }

    private fun extractFaceSamples(response: FaceCaptureResponse) =
        response.capturingResult.mapNotNull { captureResult ->
            captureResult.result?.let {
                FaceSample(it.template)
            }
        }

    private suspend fun addPersonCreationEventForFingerprintSamples(fingerprintSamples: List<FingerprintSample>) {
        ignoreException {
            sessionRepository.updateCurrentSession {
                val event = PersonCreationEvent.build(timeHelper, it, fingerprintSamples, faceSamples = null)
                it.addEvent(event)
            }
        }
    }

    private suspend fun addPersonCreationEventForFaceSamples(faceSamples: List<FaceSample>) {
        ignoreException {
            sessionRepository.updateCurrentSession {
                val event = PersonCreationEvent.build(timeHelper, it, fingerprintSamples = null, faceSamples = faceSamples)
                it.addEvent(event)
            }
        }
    }

}
