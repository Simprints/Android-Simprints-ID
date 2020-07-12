package com.simprints.id.orchestrator.modality

import android.content.Intent
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.modality.Modality
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
import com.simprints.id.tools.TimeHelper

class ModalityFlowEnrolImpl(private val fingerprintStepProcessor: FingerprintStepProcessor,
                            private val faceStepProcessor: FaceStepProcessor,
                            private val coreStepProcessor: CoreStepProcessor,
                            timeHelper: TimeHelper,
                            sessionRepository: SessionRepository,
                            consentRequired: Boolean,
                            locationRequired: Boolean,
                            private val isEnrolmentPlus: Boolean,
                            private val matchGroup: GROUP) :
    ModalityFlowBaseImpl(coreStepProcessor, fingerprintStepProcessor, faceStepProcessor, timeHelper, sessionRepository, consentRequired, locationRequired) {

    override fun startFlow(appRequest: AppRequest,
                           modalities: List<Modality>) {
        require(appRequest is AppEnrolRequest)
        addModalityConfigurationSteps(modalities)
        addSetupStep()
        addCoreConsentStepIfRequired(ENROL)
        steps.addAll(buildStepsList(modalities))
    }

    private fun buildStepsList(modalities: List<Modality>) =
        modalities.map {
            when (it) {
                Modality.FINGER -> fingerprintStepProcessor.buildStepToCapture()
                Modality.FACE -> faceStepProcessor.buildCaptureStep()
            }
        }

    override fun getNextStepToLaunch(): Step? = steps.firstOrNull { it.getStatus() == NOT_STARTED }

    override suspend fun handleIntentResult(appRequest: AppRequest, requestCode: Int, resultCode: Int, data: Intent?): Step? {
        require(appRequest is AppEnrolRequest)
        val result = when {
            isCoreResult(requestCode) -> coreStepProcessor.processResult(data)
            isFingerprintResult(requestCode) -> {
                fingerprintStepProcessor.processResult(requestCode, resultCode, data).also {
                    addEventIfFingerprintCaptureResponse(it)
                }
            }
            isFaceResult(requestCode) -> faceStepProcessor.processResult(requestCode, resultCode, data).also {
                addEventIfFaceCaptureResponse(it)
            }
            else -> throw IllegalStateException("Invalid result from intent")
        }
        completeAllStepsIfExitFormHappened(requestCode, resultCode, data)

        val stepForRequest = steps.firstOrNull { it.requestCode == requestCode }
        return stepForRequest?.apply { setResult(result) }.also {
            if (isEnrolmentPlus) {
                with(appRequest) {
                    buildQueryAndAddMatchingStepIfRequired(result, projectId, userId, moduleId)
                }
            }
        }
    }

    private suspend fun addEventIfFingerprintCaptureResponse(it: Step.Result?) {
        if (it is FingerprintCaptureResponse) {
            extractFingerprintAndAddPersonCreationEvent(it)
        }
    }

    private suspend fun addEventIfFaceCaptureResponse(response: Step.Result?) {
        if (response is FaceCaptureResponse)
            extractFaceAndAddPersonCreationEvent(response)
    }

    private fun buildQueryAndAddMatchingStepIfRequired(result: Step.Result?, projectId: String, userId: String, moduleId: String) {
        if (result is FingerprintCaptureResponse) {
            val query = buildQuery(projectId, userId, moduleId, matchGroup)
            addMatchingStepForFinger(result.captureResult.mapNotNull { it.sample }, query)
        } else if (result is FaceCaptureResponse) {
            val query = buildQuery(projectId, userId, moduleId, matchGroup)
            addMatchingStepForFace(result.capturingResult.mapNotNull { it.result }, query)
        }
    }

    private fun buildQuery(projectId: String, userId: String, moduleId: String, matchGroup: GROUP): SubjectLocalDataSource.Query =
        when (matchGroup) {
            GROUP.GLOBAL -> SubjectLocalDataSource.Query(projectId)
            GROUP.USER -> SubjectLocalDataSource.Query(projectId, attendantId = userId)
            GROUP.MODULE -> SubjectLocalDataSource.Query(projectId, moduleId = moduleId)
        }

    private fun addMatchingStepForFinger(probeSamples: List<FingerprintCaptureSample>, query: SubjectLocalDataSource.Query) {
        steps.add(fingerprintStepProcessor.buildStepToMatch(probeSamples, query))
    }

    private fun addMatchingStepForFace(probeSamples: List<FaceCaptureSample>, query: SubjectLocalDataSource.Query) {
        steps.add(faceStepProcessor.buildStepMatch(probeSamples, query))
    }
}
