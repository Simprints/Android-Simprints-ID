package com.simprints.infra.events.event.domain.models.callback

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.EventLabels
import com.simprints.infra.events.event.domain.models.EventType.CALLBACK_ERROR
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.Companion.fromAppResponseErrorReasonToEventReason
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason as ErrorReason
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_ENDED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.moduleapi.app.responses.IAppErrorReason
import org.junit.Test

class ErrorCallbackEventTest {

    @Test
    fun create_ErrorCallbackEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val event = ErrorCallbackEvent(CREATED_AT, ErrorReason.DIFFERENT_PROJECT_ID_SIGNED_IN, labels)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(CALLBACK_ERROR)
        with(event.payload as ErrorCallbackPayload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(ErrorCallbackEvent.EVENT_VERSION)
            assertThat(type).isEqualTo(CALLBACK_ERROR)
            assertThat(reason).isEqualTo(ErrorReason.DIFFERENT_PROJECT_ID_SIGNED_IN)
        }
    }

    @Test
    fun create_BackendErrorCallbackEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val event = ErrorCallbackEvent(CREATED_AT, ErrorReason.BACKEND_MAINTENANCE_ERROR, labels)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(CALLBACK_ERROR)
        with(event.payload as ErrorCallbackPayload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(ErrorCallbackEvent.EVENT_VERSION)
            assertThat(type).isEqualTo(CALLBACK_ERROR)
            assertThat(reason).isEqualTo(ErrorReason.BACKEND_MAINTENANCE_ERROR)
        }
    }

    @Test
    fun `should map AppErrorReason correctly`() {
        mapOf(
            IAppErrorReason.DIFFERENT_PROJECT_ID_SIGNED_IN to ErrorReason.DIFFERENT_PROJECT_ID_SIGNED_IN,
            IAppErrorReason.DIFFERENT_USER_ID_SIGNED_IN to ErrorReason.DIFFERENT_USER_ID_SIGNED_IN,
            IAppErrorReason.GUID_NOT_FOUND_ONLINE to ErrorReason.GUID_NOT_FOUND_ONLINE,
            IAppErrorReason.UNEXPECTED_ERROR to ErrorReason.UNEXPECTED_ERROR,
            IAppErrorReason.BLUETOOTH_NOT_SUPPORTED to ErrorReason.BLUETOOTH_NOT_SUPPORTED,
            IAppErrorReason.LOGIN_NOT_COMPLETE to ErrorReason.LOGIN_NOT_COMPLETE,
            IAppErrorReason.ENROLMENT_LAST_BIOMETRICS_FAILED to ErrorReason.ENROLMENT_LAST_BIOMETRICS_FAILED,
            IAppErrorReason.FACE_LICENSE_MISSING to ErrorReason.FACE_LICENSE_MISSING,
            IAppErrorReason.FACE_LICENSE_INVALID to ErrorReason.FACE_LICENSE_INVALID,
            IAppErrorReason.FINGERPRINT_CONFIGURATION_ERROR to ErrorReason.FINGERPRINT_CONFIGURATION_ERROR,
            IAppErrorReason.FACE_CONFIGURATION_ERROR to ErrorReason.FACE_CONFIGURATION_ERROR,
            IAppErrorReason.BACKEND_MAINTENANCE_ERROR to ErrorReason.BACKEND_MAINTENANCE_ERROR,
            IAppErrorReason.PROJECT_PAUSED to ErrorReason.PROJECT_PAUSED,
            IAppErrorReason.PROJECT_ENDING to ErrorReason.PROJECT_ENDING,
            IAppErrorReason.BLUETOOTH_NO_PERMISSION to ErrorReason.BLUETOOTH_NO_PERMISSION,
        ).forEach {
            assertThat(fromAppResponseErrorReasonToEventReason(it.key)).isEqualTo(it.value)
        }
    }
}
