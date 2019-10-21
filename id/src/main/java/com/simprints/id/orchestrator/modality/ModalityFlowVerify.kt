package com.simprints.id.orchestrator.modality

import android.content.Intent
import com.simprints.id.data.db.person.domain.FingerprintSample
import com.simprints.id.data.db.person.local.PersonLocalDataSource.Query
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.domain.modality.Modality.FINGER
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.id.domain.moduleapi.core.response.FetchGUIDResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.NOT_STARTED
import com.simprints.id.orchestrator.steps.core.CoreRequestCode.Companion.isCoreResult
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.Companion.isFaceResult
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintRequestCode.Companion.isFingerprintResult
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessor

class ModalityFlowVerifyImpl(private val fingerprintStepProcessor: FingerprintStepProcessor,
                             private val faceStepProcessor: FaceStepProcessor,
                             private val coreStepProcessor: CoreStepProcessor,
                             private val prefs: PreferencesManager) : ModalityFlowBaseImpl(coreStepProcessor) {

    override fun startFlow(appRequest: AppRequest, modalities: List<Modality>) {
        require(appRequest is AppVerifyRequest)
        super.startFlow(appRequest, modalities)
        steps.addAll(buildStepsList(appRequest, modalities))
    }

    private fun buildStepsList(appRequest: AppVerifyRequest, modalities: List<Modality>) =
        modalities.map {
            with(appRequest) {
                when (it) {
                    FINGER -> fingerprintStepProcessor.buildStepToCapture(projectId, userId, projectId, metadata)
                    FACE -> faceStepProcessor.buildCaptureStep()
                }
            }
        }

    override fun getNextStepToLaunch(): Step? = steps.firstOrNull { it.getStatus() == NOT_STARTED }

    override fun handleIntentResult(appRequest: AppRequest, requestCode: Int, resultCode: Int, data: Intent?): Step? {
        require(appRequest is AppVerifyRequest)

        val result = when {
            isCoreResult(requestCode) -> {
                coreStepProcessor.processResult(data).also {
                    completeAllStepsIfFetchGuidResponseAndFailed(it)
                }
            }
            isFingerprintResult(requestCode) -> fingerprintStepProcessor.processResult(requestCode, resultCode, data)
            isFaceResult(requestCode) -> faceStepProcessor.processResult(requestCode, resultCode, data)
            else -> throw IllegalStateException("Invalid result from intent")
        }
        completeAllStepsIfExitFormHappened(data)

        val stepRequested = steps.firstOrNull { it.requestCode == requestCode }
        stepRequested?.setResult(result)

        return stepRequested.also {
            if (result is FingerprintCaptureResponse) {
                val query = buildQuery(appRequest, prefs.matchGroup)
                addMatchingStep(result.captureResult.mapNotNull { it.sample }, query)
            }
        }
    }

    private fun buildQuery(appRequest: AppVerifyRequest, matchGroup: GROUP): Query =
        with(appRequest) {
            when (matchGroup) {
                GROUP.GLOBAL -> Query(projectId, patientId = verifyGuid)
                GROUP.USER -> Query(projectId, userId = userId, patientId = verifyGuid)
                GROUP.MODULE -> Query(projectId, moduleId = moduleId, patientId = verifyGuid)
            }
        }

    private fun addMatchingStep(probeSamples: List<FingerprintSample>, query: Query) {
        steps.add(fingerprintStepProcessor.buildStepToMatch(probeSamples, query))
    }

    private fun completeAllStepsIfFetchGuidResponseAndFailed(result: Step.Result?) {
        if (result is FetchGUIDResponse) {
            if (!result.isGuidFound) {
                steps.forEach { it.setStatus(Step.Status.COMPLETED) }
            }
        }
    }
}
