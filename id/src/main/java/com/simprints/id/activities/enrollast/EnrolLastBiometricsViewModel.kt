package com.simprints.id.activities.enrollast

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.id.activities.enrollast.EnrolLastBiometricsActivity.ViewState
import com.simprints.id.activities.enrollast.EnrolLastBiometricsActivity.ViewState.Failed
import com.simprints.id.activities.enrollast.EnrolLastBiometricsActivity.ViewState.Success
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.orchestrator.EnrolmentHelper
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.requests.EnrolLastBiometricsRequest
import com.simprints.id.orchestrator.steps.core.response.EnrolLastBiometricsResponse
import com.simprints.id.tools.TimeHelper
import timber.log.Timber

class EnrolLastBiometricsViewModel(private val enrolmentHelper: EnrolmentHelper,
                                   val timeHelper: TimeHelper) : ViewModel() {

    fun getViewStateLiveData(): LiveData<ViewState> = viewStateLiveData
    private val viewStateLiveData = MutableLiveData<ViewState>()

    suspend fun getNextStep(enrolLastBiometricsRequest: EnrolLastBiometricsRequest) {
        with(enrolLastBiometricsRequest) {
            viewStateLiveData.value = try {
                val steps = enrolLastBiometricsRequest.steps
                val previousLastEnrolmentResult = steps.firstOrNull { it.request is EnrolLastBiometricsRequest }?.getResult()
                if (previousLastEnrolmentResult is EnrolLastBiometricsResponse) {
                    previousLastEnrolmentResult.newSubjectId?.let { Success(it) } ?: Failed
                } else {
                    val person = buildPerson(steps)
                    enrolmentHelper.enrol(person)
                    Success(person.patientId)
                }
            } catch (t: Throwable) {
                Timber.e(t)
                Failed
            }
        }
    }

    private fun EnrolLastBiometricsRequest.buildPerson(steps: List<Step>): Person {
        return enrolmentHelper.buildPerson(
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
