package com.simprints.infra.eventsync.event.remote.events.callback

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason as ErrorReason
import com.simprints.infra.eventsync.event.remote.models.callback.ApiErrorCallback
import com.simprints.infra.eventsync.event.remote.models.callback.ApiErrorCallback.ApiReason.*
import com.simprints.infra.eventsync.event.remote.models.callback.fromApiToDomain
import com.simprints.infra.eventsync.event.remote.models.callback.fromDomainToApi
import org.junit.Test

class ApiErrorCallbackTest {


    @Test
    fun `ApiReason correctly mapped to domain`() {
        mapOf(
            ApiErrorCallback.ApiReason.DIFFERENT_PROJECT_ID_SIGNED_IN to DIFFERENT_PROJECT_ID_SIGNED_IN,
            ApiErrorCallback.ApiReason.DIFFERENT_USER_ID_SIGNED_IN to DIFFERENT_USER_ID_SIGNED_IN,
            ApiErrorCallback.ApiReason.GUID_NOT_FOUND_ONLINE to GUID_NOT_FOUND_ONLINE,
            GUID_NOT_FOUND_OFFLINE to UNEXPECTED_ERROR,
            ApiErrorCallback.ApiReason.UNEXPECTED_ERROR to UNEXPECTED_ERROR,
            ApiErrorCallback.ApiReason.BLUETOOTH_NOT_SUPPORTED to BLUETOOTH_NOT_SUPPORTED,
            SCANNER_LOW_BATTERY to UNEXPECTED_ERROR,
            ApiErrorCallback.ApiReason.LOGIN_NOT_COMPLETE to LOGIN_NOT_COMPLETE,
            ApiErrorCallback.ApiReason.ENROLMENT_LAST_BIOMETRICS_FAILED to ENROLMENT_LAST_BIOMETRICS_FAILED,
            ApiErrorCallback.ApiReason.FACE_LICENSE_MISSING to FACE_LICENSE_MISSING,
            ApiErrorCallback.ApiReason.FACE_LICENSE_INVALID to FACE_LICENSE_INVALID,
            ApiErrorCallback.ApiReason.BACKEND_MAINTENANCE_ERROR to BACKEND_MAINTENANCE_ERROR,
            ApiErrorCallback.ApiReason.PROJECT_ENDING to PROJECT_ENDING,
            ApiErrorCallback.ApiReason.PROJECT_PAUSED to PROJECT_PAUSED,
            ApiErrorCallback.ApiReason.BLUETOOTH_NO_PERMISSION to BLUETOOTH_NO_PERMISSION
        ).forEach {
            assertThat(it.key.fromApiToDomain()).isEqualTo(it.value)
        }
    }

    @Test
    fun `Reason correctly mapped to ApiReason`() {
        mapOf(
            ErrorReason.DIFFERENT_PROJECT_ID_SIGNED_IN to ApiErrorCallback.ApiReason.DIFFERENT_PROJECT_ID_SIGNED_IN,
            ErrorReason.DIFFERENT_USER_ID_SIGNED_IN to ApiErrorCallback.ApiReason.DIFFERENT_USER_ID_SIGNED_IN,
            ErrorReason.GUID_NOT_FOUND_ONLINE to ApiErrorCallback.ApiReason.GUID_NOT_FOUND_ONLINE,
            ErrorReason.UNEXPECTED_ERROR to ApiErrorCallback.ApiReason.UNEXPECTED_ERROR,
            ErrorReason.BLUETOOTH_NOT_SUPPORTED to ApiErrorCallback.ApiReason.BLUETOOTH_NOT_SUPPORTED,
            ErrorReason.LOGIN_NOT_COMPLETE to ApiErrorCallback.ApiReason.LOGIN_NOT_COMPLETE,
            ErrorReason.ENROLMENT_LAST_BIOMETRICS_FAILED to ApiErrorCallback.ApiReason.ENROLMENT_LAST_BIOMETRICS_FAILED,
            ErrorReason.FACE_LICENSE_MISSING to ApiErrorCallback.ApiReason.FACE_LICENSE_MISSING,
            ErrorReason.FACE_LICENSE_INVALID to ApiErrorCallback.ApiReason.FACE_LICENSE_INVALID,
            ErrorReason.FINGERPRINT_CONFIGURATION_ERROR to ApiErrorCallback.ApiReason.UNEXPECTED_ERROR,
            ErrorReason.FACE_CONFIGURATION_ERROR to ApiErrorCallback.ApiReason.UNEXPECTED_ERROR,
            ErrorReason.BACKEND_MAINTENANCE_ERROR to ApiErrorCallback.ApiReason.BACKEND_MAINTENANCE_ERROR,
            ErrorReason.PROJECT_ENDING to ApiErrorCallback.ApiReason.PROJECT_ENDING,
            ErrorReason.PROJECT_PAUSED to ApiErrorCallback.ApiReason.PROJECT_PAUSED,
            ErrorReason.BLUETOOTH_NO_PERMISSION to ApiErrorCallback.ApiReason.BLUETOOTH_NO_PERMISSION,
        ).forEach {
            assertThat(it.key.fromDomainToApi()).isEqualTo(it.value)
        }
    }
}
