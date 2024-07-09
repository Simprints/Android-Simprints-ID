package com.simprints.infra.events.event.domain.models.callback

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.infra.events.event.domain.models.EventType.CALLBACK_ERROR
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.Companion.fromAppResponseErrorReasonToEventReason
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import org.junit.Test
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason as ErrorReason

class ErrorCallbackEventTest {

    @Test
    fun create_ErrorCallbackEvent() {
        val event = ErrorCallbackEvent(CREATED_AT, ErrorReason.DIFFERENT_PROJECT_ID_SIGNED_IN)
        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(CALLBACK_ERROR)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isNull()
            assertThat(eventVersion).isEqualTo(ErrorCallbackEvent.EVENT_VERSION)
            assertThat(type).isEqualTo(CALLBACK_ERROR)
            assertThat(reason).isEqualTo(ErrorReason.DIFFERENT_PROJECT_ID_SIGNED_IN)
        }
    }

    @Test
    fun create_BackendErrorCallbackEvent() {
        val event = ErrorCallbackEvent(CREATED_AT, ErrorReason.BACKEND_MAINTENANCE_ERROR)
        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(CALLBACK_ERROR)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isNull()
            assertThat(eventVersion).isEqualTo(ErrorCallbackEvent.EVENT_VERSION)
            assertThat(type).isEqualTo(CALLBACK_ERROR)
            assertThat(reason).isEqualTo(ErrorReason.BACKEND_MAINTENANCE_ERROR)
        }
    }

    @Test
    fun `should map AppErrorReason correctly`() {
        mapOf(
            AppErrorReason.DIFFERENT_PROJECT_ID_SIGNED_IN to ErrorReason.DIFFERENT_PROJECT_ID_SIGNED_IN,
            AppErrorReason.DIFFERENT_USER_ID_SIGNED_IN to ErrorReason.DIFFERENT_USER_ID_SIGNED_IN,
            AppErrorReason.GUID_NOT_FOUND_ONLINE to ErrorReason.GUID_NOT_FOUND_ONLINE,
            AppErrorReason.GUID_NOT_FOUND_OFFLINE to ErrorReason.GUID_NOT_FOUND_OFFLINE,
            AppErrorReason.UNEXPECTED_ERROR to ErrorReason.UNEXPECTED_ERROR,
            AppErrorReason.BLUETOOTH_NOT_SUPPORTED to ErrorReason.BLUETOOTH_NOT_SUPPORTED,
            AppErrorReason.LOGIN_NOT_COMPLETE to ErrorReason.LOGIN_NOT_COMPLETE,
            AppErrorReason.ENROLMENT_LAST_BIOMETRICS_FAILED to ErrorReason.ENROLMENT_LAST_BIOMETRICS_FAILED,
            AppErrorReason.LICENSE_MISSING to ErrorReason.LICENSE_MISSING,
            AppErrorReason.LICENSE_INVALID to ErrorReason.LICENSE_INVALID,
            AppErrorReason.FINGERPRINT_CONFIGURATION_ERROR to ErrorReason.FINGERPRINT_CONFIGURATION_ERROR,
            AppErrorReason.FACE_CONFIGURATION_ERROR to ErrorReason.FACE_CONFIGURATION_ERROR,
            AppErrorReason.BACKEND_MAINTENANCE_ERROR to ErrorReason.BACKEND_MAINTENANCE_ERROR,
            AppErrorReason.PROJECT_PAUSED to ErrorReason.PROJECT_PAUSED,
            AppErrorReason.PROJECT_ENDING to ErrorReason.PROJECT_ENDING,
            AppErrorReason.BLUETOOTH_NO_PERMISSION to ErrorReason.BLUETOOTH_NO_PERMISSION,
            AppErrorReason.AGE_GROUP_NOT_SUPPORTED to ErrorReason.AGE_GROUP_NOT_SUPPORTED,
        ).forEach {
            assertThat(fromAppResponseErrorReasonToEventReason(it.key)).isEqualTo(it.value)
        }
    }
}
