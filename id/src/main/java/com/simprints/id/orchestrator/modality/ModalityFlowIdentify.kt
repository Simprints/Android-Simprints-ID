package com.simprints.id.orchestrator.modality

import android.content.Intent
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource.Query
import com.simprints.id.data.db.event.SessionRepository
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureSample
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureSample
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.NOT_STARTED
import com.simprints.id.orchestrator.steps.core.CoreRequestCode.Companion.isCoreResult
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor
import com.simprints.id.orchestrator.steps.core.requests.ConsentType
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.Companion.isFaceResult
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintRequestCode.Companion.isFingerprintResult
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessor
import com.simprints.id.tools.TimeHelper

class ModalityFlowIdentifyImpl(private val fingerprintStepProcessor: FingerprintStepProcessor,
                               private val faceStepProcessor: FaceStepProcessor,
                               private val coreStepProcessor: CoreStepProcessor,
                               private val matchGroup: GROUP,
                               timeHelper: TimeHelper,
                               sessionRepository: SessionRepository,
                               consentRequired: Boolean,
                               locationRequired: Boolean) :
    ModalityFlowBaseImpl(coreStepProcessor, fingerprintStepProcessor, faceStepProcessor, timeHelper, sessionRepository, consentRequired, locationRequired) {

    override val steps: MutableList<Step> = mutableListOf()

    override fun startFlow(appRequest: AppRequest, modalities: List<Modality>) {
        require(appRequest is AppIdentifyRequest)
        addSetupStep()
        addCoreConsentStepIfRequired(ConsentType.VERIFY)
        steps.addAll(buildStepsList(modalities))
    }

    private fun buildStepsList(modalities: List<Modality>) =
        modalities.map {
            when (it) {
                Modality.FINGER -> fingerprintStepProcessor.buildStepToCapture()
                Modality.FACE -> faceStepProcessor.buildCaptureStep()
            }
        }

    override fun getNextStepToLaunch(): Step? =
        steps.firstOrNull { it.getStatus() == NOT_STARTED }

    override suspend fun handleIntentResult(appRequest: AppRequest, requestCode: Int, resultCode: Int, data: Intent?): Step? {
        require(appRequest is AppIdentifyRequest)
        val result = when {
            isCoreResult(requestCode) -> coreStepProcessor.processResult(data)
            isFingerprintResult(requestCode) -> fingerprintStepProcessor.processResult(requestCode, resultCode, data)
            isFaceResult(requestCode) -> faceStepProcessor.processResult(requestCode, resultCode, data)
            else -> throw IllegalStateException("Invalid result from intent")
        }

        completeAllStepsIfExitFormHappened(requestCode, resultCode, data)

        val stepRequested = steps.firstOrNull { it.requestCode == requestCode }
        stepRequested?.setResult(result)

        return stepRequested.also {
            buildQueryAndAddMatchingStepIfRequired(result, appRequest)
        }
    }

    private suspend fun buildQueryAndAddMatchingStepIfRequired(result: Step.Result?, appRequest: AppIdentifyRequest) {
        if (result is FingerprintCaptureResponse) {
            val query = buildQuery(appRequest, matchGroup)
            addMatchingStepForFinger(result.captureResult.mapNotNull { it.sample }, query)
            extractFingerprintAndAddPersonCreationEvent(result)
        } else if (result is FaceCaptureResponse) {
            val query = buildQuery(appRequest, matchGroup)
            addMatchingStepForFace(result.capturingResult.mapNotNull { it.result }, query)
        }
    }

    private fun buildQuery(appRequest: AppIdentifyRequest, matchGroup: GROUP): Query =
        with(appRequest) {
            when (matchGroup) {
                GROUP.GLOBAL -> Query(projectId)
                GROUP.USER -> Query(projectId, attendantId = userId)
                GROUP.MODULE -> Query(projectId, moduleId = moduleId)
            }
        }

    private fun addMatchingStepForFinger(probeSamples: List<FingerprintCaptureSample>, query: Query) {
        steps.add(fingerprintStepProcessor.buildStepToMatch(probeSamples, query))
    }

    private fun addMatchingStepForFace(probeSamples: List<FaceCaptureSample>, query: Query) {
        steps.add(faceStepProcessor.buildStepMatch(probeSamples, query))
    }
}
