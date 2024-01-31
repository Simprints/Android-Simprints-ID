package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppEnrolResponse
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse.Reason.ENROLMENT_LAST_BIOMETRICS_FAILED
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.response.EnrolLastBiometricsResponse
import com.simprints.infra.config.store.models.GeneralConfiguration

class AppResponseBuilderForEnrolLastBiometrics : BaseAppResponseBuilder() {

    override suspend fun buildAppResponse(
        modalities: List<GeneralConfiguration.Modality>,
        appRequest: AppRequest,
        steps: List<Step>,
        sessionId: String
    ): AppResponse {

        val results = steps.map { it.getResult() }
        val responseForEnrolLastBiometrics = getCoreResponseForEnrolLastBiometrics(results)
        return responseForEnrolLastBiometrics?.newSubjectId?.let {
            AppEnrolResponse(it)
        } ?: AppErrorResponse(ENROLMENT_LAST_BIOMETRICS_FAILED)
    }

    private fun getCoreResponseForEnrolLastBiometrics(results: List<Step.Result?>): EnrolLastBiometricsResponse? =
        results.filterIsInstance(EnrolLastBiometricsResponse::class.java).lastOrNull()
}