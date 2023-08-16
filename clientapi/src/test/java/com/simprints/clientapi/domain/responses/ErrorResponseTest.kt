package com.simprints.clientapi.domain.responses

import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.domain.responses.ErrorResponse.Reason.*
import com.simprints.clientapi.domain.responses.ErrorResponse.Reason.Companion.fromAlertTypeToDomain
import com.simprints.clientapi.domain.responses.ErrorResponse.Reason.Companion.fromModuleApiToDomain
import com.simprints.clientapi.errors.ClientApiAlert
import com.simprints.moduleapi.app.responses.IAppErrorReason
import org.junit.Test

class ErrorResponseTest {

    @Test
    fun `should from module api to domain correctly`() {
        mapOf(
            IAppErrorReason.DIFFERENT_PROJECT_ID_SIGNED_IN to DIFFERENT_PROJECT_ID_SIGNED_IN,
            IAppErrorReason.DIFFERENT_USER_ID_SIGNED_IN to DIFFERENT_USER_ID_SIGNED_IN,
            IAppErrorReason.GUID_NOT_FOUND_ONLINE to GUID_NOT_FOUND_ONLINE,
            IAppErrorReason.UNEXPECTED_ERROR to UNEXPECTED_ERROR,
            IAppErrorReason.BLUETOOTH_NOT_SUPPORTED to BLUETOOTH_NOT_SUPPORTED,
            IAppErrorReason.LOGIN_NOT_COMPLETE to LOGIN_NOT_COMPLETE,
            IAppErrorReason.ROOTED_DEVICE to ROOTED_DEVICE,
            IAppErrorReason.ENROLMENT_LAST_BIOMETRICS_FAILED to ENROLMENT_LAST_BIOMETRICS_FAILED,
            IAppErrorReason.FACE_LICENSE_MISSING to FACE_LICENSE_MISSING,
            IAppErrorReason.FACE_LICENSE_INVALID to FACE_LICENSE_INVALID,
            IAppErrorReason.FINGERPRINT_CONFIGURATION_ERROR to FINGERPRINT_CONFIGURATION_ERROR,
            IAppErrorReason.FACE_CONFIGURATION_ERROR to FACE_CONFIGURATION_ERROR,
            IAppErrorReason.BACKEND_MAINTENANCE_ERROR to BACKEND_MAINTENANCE_ERROR,
            IAppErrorReason.PROJECT_PAUSED to PROJECT_PAUSED,
            IAppErrorReason.PROJECT_ENDING to PROJECT_ENDING,
            IAppErrorReason.BLUETOOTH_NO_PERMISSION to BLUETOOTH_NO_PERMISSION
        ).forEach {
            assertThat(fromModuleApiToDomain(it.key)).isEqualTo(it.value)
        }
    }

    @Test
    fun `should from alert type to domain correctly`() {
        mapOf(
            ClientApiAlert.INVALID_METADATA to INVALID_METADATA,
            ClientApiAlert.INVALID_MODULE_ID to INVALID_MODULE_ID,
            ClientApiAlert.INVALID_PROJECT_ID to INVALID_PROJECT_ID,
            ClientApiAlert.INVALID_SELECTED_ID to INVALID_SELECTED_ID,
            ClientApiAlert.INVALID_SESSION_ID to INVALID_SESSION_ID,
            ClientApiAlert.INVALID_USER_ID to INVALID_USER_ID,
            ClientApiAlert.INVALID_VERIFY_ID to INVALID_VERIFY_ID,
            ClientApiAlert.ROOTED_DEVICE to ROOTED_DEVICE,
            ClientApiAlert.INVALID_STATE_FOR_INTENT_ACTION to INVALID_STATE_FOR_INTENT_ACTION
        ).forEach {
            assertThat(fromAlertTypeToDomain(it.key)).isEqualTo(it.value)
        }
    }
}
