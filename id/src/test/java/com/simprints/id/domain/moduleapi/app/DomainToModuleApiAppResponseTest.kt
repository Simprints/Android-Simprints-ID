package com.simprints.id.domain.moduleapi.app

import com.google.common.truth.Truth.assertThat
import com.simprints.id.domain.moduleapi.app.DomainToModuleApiAppResponse.fromDomainToModuleApiAppErrorResponse
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.moduleapi.app.responses.IAppErrorReason
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class DomainToModuleApiAppResponseTest {

    @Test
    fun fromDomainToModuleApiAppErrorReason_backendMaintenanceError_shouldMapCorrectly() {
        val appErrorResponse = AppErrorResponse(AppErrorResponse.Reason.BACKEND_MAINTENANCE_ERROR)

        val response = fromDomainToModuleApiAppErrorResponse(appErrorResponse)

        assertThat(response.reason).isInstanceOf(IAppErrorReason.BACKEND_MAINTENANCE_ERROR::class.java)
    }

    @Test
    fun fromDomainToModuleApiAppErrorReason_differentProjectSignedInError_shouldMapCorrectly() {
        val appErrorResponse =
            AppErrorResponse(AppErrorResponse.Reason.DIFFERENT_PROJECT_ID_SIGNED_IN)

        val response = fromDomainToModuleApiAppErrorResponse(appErrorResponse)

        assertThat(response.reason).isInstanceOf(IAppErrorReason.DIFFERENT_PROJECT_ID_SIGNED_IN::class.java)
    }

    @Test
    fun `maps domain refusal reason to fingerprint form correctly`() {
        mapOf(
            AppErrorResponse.Reason.DIFFERENT_PROJECT_ID_SIGNED_IN to IAppErrorReason.DIFFERENT_PROJECT_ID_SIGNED_IN,
            AppErrorResponse.Reason.DIFFERENT_USER_ID_SIGNED_IN to IAppErrorReason.DIFFERENT_USER_ID_SIGNED_IN,
            AppErrorResponse.Reason.UNEXPECTED_ERROR to IAppErrorReason.UNEXPECTED_ERROR,
            AppErrorResponse.Reason.LOGIN_NOT_COMPLETE to IAppErrorReason.LOGIN_NOT_COMPLETE,
            AppErrorResponse.Reason.BLUETOOTH_NOT_SUPPORTED to IAppErrorReason.BLUETOOTH_NOT_SUPPORTED,
            AppErrorResponse.Reason.GUID_NOT_FOUND_ONLINE to IAppErrorReason.GUID_NOT_FOUND_ONLINE,
            AppErrorResponse.Reason.GUID_NOT_FOUND_OFFLINE to IAppErrorReason.GUID_NOT_FOUND_OFFLINE,
            AppErrorResponse.Reason.ENROLMENT_LAST_BIOMETRICS_FAILED to IAppErrorReason.ENROLMENT_LAST_BIOMETRICS_FAILED,
            AppErrorResponse.Reason.FACE_LICENSE_MISSING to IAppErrorReason.FACE_LICENSE_MISSING,
            AppErrorResponse.Reason.FACE_LICENSE_INVALID to IAppErrorReason.FACE_LICENSE_INVALID,
            AppErrorResponse.Reason.FINGERPRINT_CONFIGURATION_ERROR to IAppErrorReason.FINGERPRINT_CONFIGURATION_ERROR,
            AppErrorResponse.Reason.BLUETOOTH_NO_PERMISSION to IAppErrorReason.BLUETOOTH_NO_PERMISSION,
            AppErrorResponse.Reason.BACKEND_MAINTENANCE_ERROR to IAppErrorReason.BACKEND_MAINTENANCE_ERROR,
            AppErrorResponse.Reason.PROJECT_ENDING to IAppErrorReason.PROJECT_ENDING,
            AppErrorResponse.Reason.PROJECT_PAUSED to IAppErrorReason.PROJECT_PAUSED,
            AppErrorResponse.Reason.FACE_CONFIGURATION_ERROR to IAppErrorReason.FACE_CONFIGURATION_ERROR
        ).forEach { (formReason, exitReason) ->
            val response: AppErrorResponse = mockk {
                every { reason } returns formReason
            }
            with(fromDomainToModuleApiAppErrorResponse(response)) {
                assertThat(reason).isEqualTo(exitReason)
            }
        }
    }
}
