package com.simprints.id.activities.enrollast

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.exceptions.validator.EnrolmentEventValidatorException
import com.simprints.id.activities.enrollast.EnrolLastBiometricsActivity.ViewState
import com.simprints.id.activities.enrollast.EnrolLastBiometricsActivity.ViewState.Failed
import com.simprints.id.activities.enrollast.EnrolLastBiometricsActivity.ViewState.Success
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceMatchResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintMatchResponse
import com.simprints.id.orchestrator.EnrolmentHelper
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.requests.EnrolLastBiometricsRequest
import com.simprints.id.orchestrator.steps.core.response.EnrolLastBiometricsResponse
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.enrolment.records.domain.models.Subject
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EnrolLastBiometricsViewModel @Inject constructor(
    private val enrolmentHelper: EnrolmentHelper,
    private val timeHelper: TimeHelper,
    private val configManager: ConfigManager
) : ViewModel() {

    fun getViewStateLiveData(): LiveData<ViewState> = viewStateLiveData
    private val viewStateLiveData = MutableLiveData<ViewState>()

    suspend fun processEnrolLastBiometricsRequest(enrolLastBiometricsRequest: EnrolLastBiometricsRequest) {
        viewStateLiveData.value = try {
            val projectConfiguration = configManager.getProjectConfiguration()
            val steps = enrolLastBiometricsRequest.previousSteps
            val previousLastEnrolmentResult =
                steps.firstOrNull { it.request is EnrolLastBiometricsRequest }?.getResult()
            if (previousLastEnrolmentResult is EnrolLastBiometricsResponse) {
                previousLastEnrolmentResult.newSubjectId?.let { Success(it) } ?: Failed
            } else {
                performEnrolmentIfRequiredAndGetViewState(
                    projectConfiguration,
                    steps,
                    enrolLastBiometricsRequest
                )
            }
        } catch (t: Throwable) {
            Simber.e(t)
            Failed
        }
    }

    private suspend fun performEnrolmentIfRequiredAndGetViewState(
        configuration: ProjectConfiguration,
        steps: List<Step>,
        enrolLastBiometricsRequest: EnrolLastBiometricsRequest
    ): ViewState {
        return if (configuration.general.duplicateBiometricEnrolmentCheck) {
            val results = steps.map { it.getResult() }
            val fingerprintResponse = getFingerprintMatchResponseFromSteps(results)
            val faceResponse = getFaceMatchResponseFromSteps(results)
            processResponsesAndGetViewState(
                configuration,
                fingerprintResponse,
                faceResponse,
                enrolLastBiometricsRequest,
                steps
            )
        } else {
            buildSubjectAndGetViewState(enrolLastBiometricsRequest, steps)
        }
    }

    private suspend fun processResponsesAndGetViewState(
        configuration: ProjectConfiguration,
        fingerprintResponse: FingerprintMatchResponse?,
        faceResponse: FaceMatchResponse?,
        enrolLastBiometricsRequest: EnrolLastBiometricsRequest,
        steps: List<Step>
    ): ViewState {
        return when {
            /**
             * We would only process the fingerprint response in a multi-modal flow until a
            proper results combining mechanism is in place
             */
            fingerprintResponse != null && faceResponse != null -> {
                if (isAnyResponseWithHighConfidence(configuration, fingerprintResponse)) {
                    Failed
                } else {
                    buildSubjectAndGetViewState(enrolLastBiometricsRequest, steps)
                }
            }
            fingerprintResponse != null -> {
                if (isAnyResponseWithHighConfidence(configuration, fingerprintResponse)) {
                    Failed
                } else {
                    buildSubjectAndGetViewState(enrolLastBiometricsRequest, steps)
                }
            }
            faceResponse != null -> {
                if (isAnyResponseWithHighConfidence(configuration, faceResponse)) {
                    Failed
                } else {
                    buildSubjectAndGetViewState(enrolLastBiometricsRequest, steps)
                }
            }
            else -> Failed
        }
    }

    private suspend fun buildSubjectAndGetViewState(
        enrolLastBiometricsRequest: EnrolLastBiometricsRequest,
        steps: List<Step>
    ): ViewState {
        return try {
            val subject = enrolLastBiometricsRequest.buildSubject(steps)
            enrolmentHelper.enrol(subject)
            Success(subject.subjectId)
        } catch (e: EnrolmentEventValidatorException) {
            Failed
        }
    }

    private fun isAnyResponseWithHighConfidence(
        configuration: ProjectConfiguration,
        fingerprintResponse: FingerprintMatchResponse
    ) =
        fingerprintResponse.result.any {
            it.confidenceScore >= configuration.fingerprint!!.decisionPolicy.high
        }

    private fun isAnyResponseWithHighConfidence(
        configuration: ProjectConfiguration,
        faceResponse: FaceMatchResponse
    ) =
        faceResponse.result.any {
            it.confidence >= configuration.face!!.decisionPolicy.high
        }

    private fun getFingerprintMatchResponseFromSteps(results: List<Step.Result?>) =
        results.filterIsInstance(FingerprintMatchResponse::class.java).lastOrNull()

    private fun getFaceMatchResponseFromSteps(results: List<Step.Result?>) =
        results.filterIsInstance(FaceMatchResponse::class.java).lastOrNull()

    private fun EnrolLastBiometricsRequest.buildSubject(steps: List<Step>): Subject {
        return enrolmentHelper.buildSubject(
            projectId,
            userId,
            moduleId,
            getCaptureResponse<FingerprintCaptureResponse>(steps),
            getCaptureResponse<FaceCaptureResponse>(steps),
            timeHelper
        )
    }

    private inline fun <reified T> getCaptureResponse(steps: List<Step>) =
        steps.firstOrNull { it.getResult() is T }?.getResult() as T?
}
