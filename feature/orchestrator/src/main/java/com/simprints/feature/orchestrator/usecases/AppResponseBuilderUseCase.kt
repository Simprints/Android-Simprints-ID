package com.simprints.feature.orchestrator.usecases

import android.os.Parcelable
import com.simprints.feature.enrollast.EnrolLastBiometricContract
import com.simprints.feature.orchestrator.model.responses.AppConfirmationResponse
import com.simprints.feature.orchestrator.model.responses.AppEnrolResponse
import com.simprints.feature.orchestrator.model.responses.AppErrorResponse
import com.simprints.feature.selectsubject.SelectSubjectContract
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.moduleapi.app.responses.IAppErrorReason
import com.simprints.moduleapi.app.responses.IAppResponse
import javax.inject.Inject

class AppResponseBuilderUseCase @Inject constructor(
) {

    operator fun invoke(
        request: ActionRequest?,
        results: List<Parcelable>,
    ): IAppResponse = when (request) {
        is ActionRequest.EnrolActionRequest -> {
            TODO()
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

    private fun buildConfirmResponse(results: List<Parcelable>): IAppResponse = results
        .filterIsInstance(SelectSubjectContract.RESULT_CLASS)
        .lastOrNull()
        ?.let { AppConfirmationResponse(true) }
        ?: AppErrorResponse(IAppErrorReason.GUID_NOT_FOUND_ONLINE)

    private fun buildLastBiometricResponse(results: List<Parcelable>) = results
        .filterIsInstance(EnrolLastBiometricContract.RESULT_CLASS)
        .lastOrNull()
        ?.newSubjectId
        ?.let { AppEnrolResponse(it) }
        ?: AppErrorResponse(IAppErrorReason.ENROLMENT_LAST_BIOMETRICS_FAILED)
}
