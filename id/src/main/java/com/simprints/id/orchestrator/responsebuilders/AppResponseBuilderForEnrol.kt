package com.simprints.id.orchestrator.responsebuilders

import com.simprints.core.domain.modality.Modality
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.exceptions.validator.EnrolmentEventValidatorException
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.responses.AppEnrolResponse
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.orchestrator.EnrolmentHelper
import com.simprints.id.orchestrator.steps.Step

class AppResponseBuilderForEnrol(
    private val enrolmentHelper: EnrolmentHelper,
    private val timeHelper: TimeHelper
) : BaseAppResponseBuilder() {

    override suspend fun buildAppResponse(
        modalities: List<Modality>,
        appRequest: AppRequest,
        steps: List<Step>,
        sessionId: String
    ): AppResponse {

        super.getErrorOrRefusalResponseIfAny(steps)?.let {
            return it
        }

        val request = appRequest as AppEnrolRequest
        val results = steps.map { it.getResult() }
        val faceResponse = getFaceCaptureResponse(results)
        val fingerprintResponse = getFingerprintCaptureResponse(results)

        val subject = enrolmentHelper.buildSubject(
            request.projectId,
            request.userId,
            request.moduleId,
            fingerprintResponse,
            faceResponse,
            timeHelper
        )

        return try {
            enrolmentHelper.enrol(subject)
            AppEnrolResponse(subject.subjectId)
        } catch (e: EnrolmentEventValidatorException) {
            AppErrorResponse(AppErrorResponse.Reason.UNEXPECTED_ERROR)
        }

    }

    private fun getFaceCaptureResponse(results: List<Step.Result?>): FaceCaptureResponse? =
        results.filterIsInstance<FaceCaptureResponse>().lastOrNull()

    private fun getFingerprintCaptureResponse(results: List<Step.Result?>): FingerprintCaptureResponse? =
        results.filterIsInstance<FingerprintCaptureResponse>().lastOrNull()
}
