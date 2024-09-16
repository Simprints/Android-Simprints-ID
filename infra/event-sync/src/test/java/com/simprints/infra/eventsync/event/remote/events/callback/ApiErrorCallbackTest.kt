package com.simprints.infra.eventsync.event.remote.events.callback

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.eventsync.event.remote.models.callback.ApiErrorCallback
import com.simprints.infra.eventsync.event.remote.models.callback.ApiErrorCallback.ApiReason.SCANNER_LOW_BATTERY
import com.simprints.infra.eventsync.event.remote.models.callback.fromApiToDomain
import com.simprints.infra.eventsync.event.remote.models.callback.fromDomainToApi
import org.junit.Test
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason as ErrorReason

class ApiErrorCallbackTest {

    @Test
    fun `ApiReason correctly mapped to domain`() {
        mapOf(
            ApiErrorCallback.ApiReason.DIFFERENT_PROJECT_ID_SIGNED_IN to ErrorReason.DIFFERENT_PROJECT_ID_SIGNED_IN,
            ApiErrorCallback.ApiReason.DIFFERENT_USER_ID_SIGNED_IN to ErrorReason.DIFFERENT_USER_ID_SIGNED_IN,
            ApiErrorCallback.ApiReason.GUID_NOT_FOUND_ONLINE to ErrorReason.GUID_NOT_FOUND_ONLINE,
            ApiErrorCallback.ApiReason.GUID_NOT_FOUND_OFFLINE to ErrorReason.GUID_NOT_FOUND_OFFLINE,
            ApiErrorCallback.ApiReason.UNEXPECTED_ERROR to ErrorReason.UNEXPECTED_ERROR,
            ApiErrorCallback.ApiReason.BLUETOOTH_NOT_SUPPORTED to ErrorReason.BLUETOOTH_NOT_SUPPORTED,
            SCANNER_LOW_BATTERY to ErrorReason.UNEXPECTED_ERROR,
            ApiErrorCallback.ApiReason.LOGIN_NOT_COMPLETE to ErrorReason.LOGIN_NOT_COMPLETE,
            ApiErrorCallback.ApiReason.ENROLMENT_LAST_BIOMETRICS_FAILED to ErrorReason.ENROLMENT_LAST_BIOMETRICS_FAILED,
            ApiErrorCallback.ApiReason.LICENSE_MISSING to ErrorReason.LICENSE_MISSING,
            ApiErrorCallback.ApiReason.LICENSE_INVALID to ErrorReason.LICENSE_INVALID,
            ApiErrorCallback.ApiReason.BACKEND_MAINTENANCE_ERROR to ErrorReason.BACKEND_MAINTENANCE_ERROR,
            ApiErrorCallback.ApiReason.PROJECT_ENDING to ErrorReason.PROJECT_ENDING,
            ApiErrorCallback.ApiReason.PROJECT_PAUSED to ErrorReason.PROJECT_PAUSED,
            ApiErrorCallback.ApiReason.BLUETOOTH_NO_PERMISSION to ErrorReason.BLUETOOTH_NO_PERMISSION,
            ApiErrorCallback.ApiReason.AGE_GROUP_NOT_SUPPORTED to ErrorReason.AGE_GROUP_NOT_SUPPORTED,
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
            ErrorReason.GUID_NOT_FOUND_OFFLINE to ApiErrorCallback.ApiReason.GUID_NOT_FOUND_OFFLINE,
            ErrorReason.UNEXPECTED_ERROR to ApiErrorCallback.ApiReason.UNEXPECTED_ERROR,
            ErrorReason.BLUETOOTH_NOT_SUPPORTED to ApiErrorCallback.ApiReason.BLUETOOTH_NOT_SUPPORTED,
            ErrorReason.LOGIN_NOT_COMPLETE to ApiErrorCallback.ApiReason.LOGIN_NOT_COMPLETE,
            ErrorReason.ENROLMENT_LAST_BIOMETRICS_FAILED to ApiErrorCallback.ApiReason.ENROLMENT_LAST_BIOMETRICS_FAILED,
            ErrorReason.LICENSE_MISSING to ApiErrorCallback.ApiReason.LICENSE_MISSING,
            ErrorReason.LICENSE_INVALID to ApiErrorCallback.ApiReason.LICENSE_INVALID,
            ErrorReason.FINGERPRINT_CONFIGURATION_ERROR to ApiErrorCallback.ApiReason.UNEXPECTED_ERROR,
            ErrorReason.FACE_CONFIGURATION_ERROR to ApiErrorCallback.ApiReason.UNEXPECTED_ERROR,
            ErrorReason.BACKEND_MAINTENANCE_ERROR to ApiErrorCallback.ApiReason.BACKEND_MAINTENANCE_ERROR,
            ErrorReason.PROJECT_ENDING to ApiErrorCallback.ApiReason.PROJECT_ENDING,
            ErrorReason.PROJECT_PAUSED to ApiErrorCallback.ApiReason.PROJECT_PAUSED,
            ErrorReason.BLUETOOTH_NO_PERMISSION to ApiErrorCallback.ApiReason.BLUETOOTH_NO_PERMISSION,
            ErrorReason.AGE_GROUP_NOT_SUPPORTED to ApiErrorCallback.ApiReason.AGE_GROUP_NOT_SUPPORTED,
        ).forEach {
            assertThat(it.key.fromDomainToApi()).isEqualTo(it.value)
        }
    }
}
