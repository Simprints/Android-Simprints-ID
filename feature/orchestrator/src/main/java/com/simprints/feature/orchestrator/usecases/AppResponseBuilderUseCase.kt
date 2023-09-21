package com.simprints.feature.orchestrator.usecases

import android.os.Parcelable
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.feature.enrollast.EnrolLastBiometricResult
import com.simprints.feature.orchestrator.model.responses.AppConfirmationResponse
import com.simprints.feature.orchestrator.model.responses.AppEnrolResponse
import com.simprints.feature.orchestrator.model.responses.AppErrorResponse
import com.simprints.feature.selectsubject.SelectSubjectResult
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.moduleapi.app.responses.IAppErrorReason
import com.simprints.moduleapi.app.responses.IAppResponse
import javax.inject.Inject

class AppResponseBuilderUseCase @Inject constructor(
    private val buildSubject: BuildEnrolledSubjectUseCase,
    private val enrolSubject: EnrolSubjectUseCase,
) {

    suspend operator fun invoke(
        request: ActionRequest?,
        results: List<Parcelable>,
    ): IAppResponse = when (request) {
        is ActionRequest.EnrolActionRequest -> {
            // TODO perform adjudication first
            handleEnrolment(results, request)
        }

        is ActionRequest.IdentifyActionRequest -> {
            TODO()
        }

        is ActionRequest.VerifyActionRequest -> {
            TODO()
        }

        is ActionRequest.ConfirmActionRequest -> buildConfirmResponse(results)
        is ActionRequest.EnrolLastBiometricActionRequest -> buildLastBiometricResponse(results)
        null -> AppErrorResponse(IAppErrorReason.UNEXPECTED_ERROR)
    }


    private suspend fun handleEnrolment(results: List<Parcelable>, request: ActionRequest.EnrolActionRequest): IAppResponse {
        val faceCapture = results.lastOrNull { it is FaceCaptureResult } as? FaceCaptureResult
        // TODO fingerprint capture
        return try {
            val subject = buildSubject(request.projectId, request.userId, request.moduleId, faceCapture)
            enrolSubject(subject)

            AppEnrolResponse(subject.subjectId)
        } catch (e: Exception) {
            e.printStackTrace()
            AppErrorResponse(IAppErrorReason.UNEXPECTED_ERROR)
        }
    }

    private fun buildConfirmResponse(results: List<Parcelable>): IAppResponse = results
        .filterIsInstance(SelectSubjectResult::class.java)
        .lastOrNull()
        ?.let { AppConfirmationResponse(true) }
        ?: AppErrorResponse(IAppErrorReason.GUID_NOT_FOUND_ONLINE)

    private fun buildLastBiometricResponse(results: List<Parcelable>) = results
        .filterIsInstance(EnrolLastBiometricResult::class.java)
        .lastOrNull()
        ?.newSubjectId
        ?.let { AppEnrolResponse(it) }
        ?: AppErrorResponse(IAppErrorReason.ENROLMENT_LAST_BIOMETRICS_FAILED)
}
