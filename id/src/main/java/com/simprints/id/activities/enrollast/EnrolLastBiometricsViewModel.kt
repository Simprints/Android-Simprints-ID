package com.simprints.id.activities.enrollast

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.id.activities.enrollast.EnrolLastBiometricsActivity.ViewState
import com.simprints.id.activities.enrollast.EnrolLastBiometricsActivity.ViewState.Failed
import com.simprints.id.activities.enrollast.EnrolLastBiometricsActivity.ViewState.Success
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceMatchResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintMatchResponse
import com.simprints.id.orchestrator.EnrolmentHelper
import com.simprints.id.orchestrator.responsebuilders.FaceConfidenceThresholds
import com.simprints.id.orchestrator.responsebuilders.FingerprintConfidenceThresholds
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.requests.EnrolLastBiometricsRequest
import com.simprints.id.orchestrator.steps.core.response.EnrolLastBiometricsResponse
import com.simprints.id.tools.time.TimeHelper
import timber.log.Timber

class EnrolLastBiometricsViewModel(private val enrolmentHelper: EnrolmentHelper,
                                   val timeHelper: TimeHelper,
                                   private val fingerprintConfidenceThresholds: Map<FingerprintConfidenceThresholds, Int>,
                                   private val faceConfidenceThresholds: Map<FaceConfidenceThresholds, Int>,
                                   private val isEnrolmentPlus: Boolean) : ViewModel() {

    fun getViewStateLiveData(): LiveData<ViewState> = viewStateLiveData
    private val viewStateLiveData = MutableLiveData<ViewState>()

    suspend fun processEnrolLastBiometricsRequest(enrolLastBiometricsRequest: EnrolLastBiometricsRequest) {
        viewStateLiveData.value = try {
            val steps = enrolLastBiometricsRequest.previousSteps
            val previousLastEnrolmentResult = steps.firstOrNull { it.request is EnrolLastBiometricsRequest }?.getResult()
            if (previousLastEnrolmentResult is EnrolLastBiometricsResponse) {
                previousLastEnrolmentResult.newSubjectId?.let { Success(it) } ?: Failed
            } else {
                performEnrolmentIfRequiredAndGetViewState(steps, enrolLastBiometricsRequest)
            }
        } catch (t: Throwable) {
            Timber.e(t)
            Failed
        }
    }

    private suspend fun performEnrolmentIfRequiredAndGetViewState(steps: List<Step>,
                                                          enrolLastBiometricsRequest: EnrolLastBiometricsRequest): ViewState {
        return if (isEnrolmentPlus) {
            val results = steps.map { it.getResult() }
            val fingerprintResponse = getFingerprintMatchResponseFromSteps(results)
            val faceResponse = getFaceMatchResponseFromSteps(results)
            processResponsesAndGetViewState(fingerprintResponse, faceResponse, enrolLastBiometricsRequest, steps)
        } else {
            buildSubjectAndGetSuccessViewState(enrolLastBiometricsRequest, steps)
        }
    }

    private suspend fun processResponsesAndGetViewState(fingerprintResponse: FingerprintMatchResponse?,
                                                        faceResponse: FaceMatchResponse?, enrolLastBiometricsRequest: EnrolLastBiometricsRequest, steps: List<Step>): ViewState {
        return when {
            /**
             * We would only process the fingerprint response in a multi-modal flow until a
            proper results combining mechanism is in place
             */
            fingerprintResponse != null && faceResponse != null -> {
                if (isAnyResponseWithHighConfidence(fingerprintResponse)) {
                    Failed
                } else {
                    buildSubjectAndGetSuccessViewState(enrolLastBiometricsRequest, steps)
                }
            }
            fingerprintResponse != null -> {
                if (isAnyResponseWithHighConfidence(fingerprintResponse)) {
                    Failed
                } else {
                    buildSubjectAndGetSuccessViewState(enrolLastBiometricsRequest, steps)
                }
            }
            faceResponse != null -> {
                if (isAnyResponseWithHighConfidence(faceResponse)) {
                    Failed
                } else {
                    buildSubjectAndGetSuccessViewState(enrolLastBiometricsRequest, steps)
                }
            }
            else -> Failed
        }
    }

    private suspend fun buildSubjectAndGetSuccessViewState(enrolLastBiometricsRequest: EnrolLastBiometricsRequest, steps: List<Step>): Success {
        val subject = enrolLastBiometricsRequest.buildSubject(steps)
        enrolmentHelper.enrol(subject)
        return Success(subject.subjectId)
    }

    private fun isAnyResponseWithHighConfidence(fingerprintResponse: FingerprintMatchResponse) =
        fingerprintResponse.result.any {
            it.confidenceScore >= fingerprintConfidenceThresholds.getValue(FingerprintConfidenceThresholds.HIGH)
        }

    private fun isAnyResponseWithHighConfidence(faceResponse: FaceMatchResponse) =
        faceResponse.result.any {
            it.confidence >= faceConfidenceThresholds.getValue(FaceConfidenceThresholds.HIGH)
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
            timeHelper)
    }

    private inline fun <reified T> getCaptureResponse(steps: List<Step>) =
        steps.firstOrNull { it.getResult() is T }?.getResult() as T?
}
