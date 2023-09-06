package com.simprints.infra.events.event.domain.validators

import com.simprints.infra.events.domain.validators.SessionCaptureEventValidator
import com.simprints.infra.events.exceptions.validator.SessionEventCaptureAlreadyExists
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.SampleDefaults.GUID2
import com.simprints.infra.events.sampledata.createSessionCaptureEvent
import com.simprints.testtools.common.syntax.assertThrows
import org.junit.Test

class SessionCaptureEventValidatorTest {

    @Test
    fun addSessionCaptureEvent_succeeds() {
        SessionCaptureEventValidator().validate(emptyList(), createSessionCaptureEvent())
    }

    @Test
    fun alreadySessionCaptureEventPresent_addNewSessionCaptureEvent_fails() {
        assertThrows<SessionEventCaptureAlreadyExists> {
            SessionCaptureEventValidator().validate(
                listOf(createSessionCaptureEvent(GUID1)),
                    createSessionCaptureEvent(GUID2)
            )
        }
    }

}
