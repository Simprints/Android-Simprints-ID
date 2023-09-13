package com.simprints.feature.clientapi.usecases

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.moduleapi.app.responses.IAppErrorReason
import com.simprints.moduleapi.app.responses.IAppErrorResponse
import javax.inject.Inject

@ExcludedFromGeneratedTestCoverageReports("Code is basically a just mapping of constants to boolean")
internal class IsFlowCompletedWithErrorUseCase @Inject constructor() {

    operator fun invoke(errorResponse: IAppErrorResponse) = when (errorResponse.reason) {
        IAppErrorReason.UNEXPECTED_ERROR,
        IAppErrorReason.DIFFERENT_PROJECT_ID_SIGNED_IN,
        IAppErrorReason.DIFFERENT_USER_ID_SIGNED_IN,
        IAppErrorReason.BLUETOOTH_NOT_SUPPORTED,
        IAppErrorReason.BLUETOOTH_NO_PERMISSION,
        IAppErrorReason.GUID_NOT_FOUND_ONLINE,
        IAppErrorReason.PROJECT_PAUSED,
        IAppErrorReason.PROJECT_ENDING,
        -> true

        IAppErrorReason.ROOTED_DEVICE,
        IAppErrorReason.ENROLMENT_LAST_BIOMETRICS_FAILED,
        IAppErrorReason.LOGIN_NOT_COMPLETE,
        IAppErrorReason.FINGERPRINT_CONFIGURATION_ERROR,
        IAppErrorReason.FACE_LICENSE_MISSING,
        IAppErrorReason.FACE_LICENSE_INVALID,
        IAppErrorReason.FACE_CONFIGURATION_ERROR,
        IAppErrorReason.BACKEND_MAINTENANCE_ERROR,
        -> false
    }
}
