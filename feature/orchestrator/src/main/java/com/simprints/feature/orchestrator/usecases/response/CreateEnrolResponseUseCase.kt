package com.simprints.feature.orchestrator.usecases.response

import com.simprints.core.domain.response.AppErrorReason
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.infra.eventsync.sync.down.tasks.SubjectFactory
import com.simprints.infra.logging.Simber
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.responses.AppEnrolResponse
import com.simprints.infra.orchestration.data.responses.AppErrorResponse
import com.simprints.infra.orchestration.data.responses.AppResponse
import java.io.Serializable
import javax.inject.Inject

internal class CreateEnrolResponseUseCase @Inject constructor(
    private val subjectFactory: SubjectFactory,
    private val enrolSubject: EnrolSubjectUseCase,
) {

    suspend operator fun invoke(request: ActionRequest.EnrolActionRequest, results: List<Serializable>): AppResponse {
        val fingerprintCapture = results.filterIsInstance(FingerprintCaptureResult::class.java).lastOrNull()
        val faceCapture = results.filterIsInstance(FaceCaptureResult::class.java).lastOrNull()

        return try {
            val subject = subjectFactory.buildSubjectFromCaptureResults(
                projectId = request.projectId,
                attendantId = request.userId,
                moduleId = request.moduleId,
                fingerprintResponse = fingerprintCapture,
                faceResponse = faceCapture
            )
            enrolSubject(subject)

            AppEnrolResponse(subject.subjectId)
        } catch (e: Exception) {
            Simber.e(e)
            AppErrorResponse(AppErrorReason.UNEXPECTED_ERROR)
        }
    }
}
