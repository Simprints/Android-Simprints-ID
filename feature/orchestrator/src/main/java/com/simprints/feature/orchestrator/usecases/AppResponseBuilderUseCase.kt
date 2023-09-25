package com.simprints.feature.orchestrator.usecases

import android.os.Parcelable
import com.simprints.feature.enrollast.EnrolLastBiometricResult
import com.simprints.feature.orchestrator.model.responses.AppConfirmationResponse
import com.simprints.feature.orchestrator.model.responses.AppEnrolResponse
import com.simprints.feature.orchestrator.model.responses.AppErrorResponse
import com.simprints.feature.selectsubject.SelectSubjectResult
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.moduleapi.app.responses.IAppErrorReason
import com.simprints.moduleapi.app.responses.IAppResponse
import javax.inject.Inject

internal class AppResponseBuilderUseCase @Inject constructor(
    private val isNewEnrolment: IsNewEnrolmentUseCase,
    private val handleEnrolment: CreateEnrolResponseUseCase,
    private val handleIdentify: CreateIdentifyResponseUseCase,
    private val handleVerify: CreateVerifyResponseUseCase,
) {

    suspend operator fun invoke(
        projectConfiguration: ProjectConfiguration,
        request: ActionRequest?,
        results: List<Parcelable>,
    ): IAppResponse = when (request) {
        is ActionRequest.EnrolActionRequest -> if (isNewEnrolment(projectConfiguration, results)) {
            handleEnrolment(request, results)
        } else {
            handleIdentify(projectConfiguration, results)
        }

        is ActionRequest.IdentifyActionRequest -> handleIdentify(projectConfiguration, results)
        is ActionRequest.VerifyActionRequest -> handleVerify(projectConfiguration, results)
        is ActionRequest.ConfirmActionRequest -> buildConfirmResponse(results)
        is ActionRequest.EnrolLastBiometricActionRequest -> buildLastBiometricResponse(results)
        null -> AppErrorResponse(IAppErrorReason.UNEXPECTED_ERROR)
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
