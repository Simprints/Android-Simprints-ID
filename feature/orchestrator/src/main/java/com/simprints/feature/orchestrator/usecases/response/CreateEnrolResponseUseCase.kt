package com.simprints.feature.orchestrator.usecases.response

import android.os.Parcelable
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.feature.orchestrator.model.responses.AppEnrolResponse
import com.simprints.feature.orchestrator.model.responses.AppErrorResponse
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.moduleapi.app.responses.IAppErrorReason
import com.simprints.moduleapi.app.responses.IAppResponse
import javax.inject.Inject

internal class CreateEnrolResponseUseCase @Inject constructor(
    private val buildSubject: BuildEnrolledSubjectUseCase,
    private val enrolSubject: EnrolSubjectUseCase,
) {

    suspend operator fun invoke(request: ActionRequest.EnrolActionRequest, results: List<Parcelable>): IAppResponse {
        val faceCapture = results.filterIsInstance(FaceCaptureResult::class.java).lastOrNull()
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
}
