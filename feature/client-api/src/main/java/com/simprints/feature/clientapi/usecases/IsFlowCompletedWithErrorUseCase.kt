package com.simprints.feature.clientapi.usecases

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.infra.orchestration.data.responses.AppErrorResponse
import javax.inject.Inject

@ExcludedFromGeneratedTestCoverageReports("Code is basically a just mapping of constants to boolean")
internal class IsFlowCompletedWithErrorUseCase @Inject constructor() {

    operator fun invoke(errorResponse: AppErrorResponse) = when (errorResponse.reason) {
        AppErrorReason.UNEXPECTED_ERROR,
        AppErrorReason.DIFFERENT_PROJECT_ID_SIGNED_IN,
        AppErrorReason.DIFFERENT_USER_ID_SIGNED_IN,
        AppErrorReason.BLUETOOTH_NOT_SUPPORTED,
        AppErrorReason.BLUETOOTH_NO_PERMISSION,
        AppErrorReason.GUID_NOT_FOUND_ONLINE,
        AppErrorReason.GUID_NOT_FOUND_OFFLINE,
        AppErrorReason.PROJECT_PAUSED,
        AppErrorReason.PROJECT_ENDING,
        AppErrorReason.AGE_GROUP_NOT_SUPPORTED,
        -> true

        AppErrorReason.ROOTED_DEVICE,
        AppErrorReason.ENROLMENT_LAST_BIOMETRICS_FAILED,
        AppErrorReason.LOGIN_NOT_COMPLETE,
        AppErrorReason.FINGERPRINT_CONFIGURATION_ERROR,
        AppErrorReason.LICENSE_MISSING,
        AppErrorReason.LICENSE_INVALID,
        AppErrorReason.FACE_CONFIGURATION_ERROR,
        AppErrorReason.BACKEND_MAINTENANCE_ERROR,
        -> false
    }
}
