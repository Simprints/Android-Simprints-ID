package com.simprints.feature.orchestrator.usecases.response

import com.simprints.core.domain.response.AppErrorReason
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.responses.AppErrorResponse
import com.simprints.infra.orchestration.data.responses.AppResponse
import java.io.Serializable
import javax.inject.Inject

internal class AppResponseBuilderUseCase @Inject constructor(
    private val isNewEnrolment: IsNewEnrolmentUseCase,
    private val handleEnrolment: CreateEnrolResponseUseCase,
    private val handleIdentify: CreateIdentifyResponseUseCase,
    private val handleVerify: CreateVerifyResponseUseCase,
    private val handleConfirmIdentity: CreateConfirmIdentityResponseUseCase,
    private val handleEnrolLastBiometric: CreateEnrolLastBiometricResponseUseCase,
) {

    suspend operator fun invoke(
        projectConfiguration: ProjectConfiguration,
        request: ActionRequest?,
        results: List<Serializable>,
    ): AppResponse = when (request) {
        is ActionRequest.EnrolActionRequest -> if (isNewEnrolment(projectConfiguration, results)) {
            handleEnrolment(request, results)
        } else {
            handleIdentify(projectConfiguration, results)
        }

        is ActionRequest.IdentifyActionRequest -> handleIdentify(projectConfiguration, results)
        is ActionRequest.VerifyActionRequest -> handleVerify(projectConfiguration, results)
        is ActionRequest.ConfirmIdentityActionRequest -> handleConfirmIdentity(results)
        is ActionRequest.EnrolLastBiometricActionRequest -> handleEnrolLastBiometric(results)
        null -> AppErrorResponse(AppErrorReason.UNEXPECTED_ERROR)
    }
}
