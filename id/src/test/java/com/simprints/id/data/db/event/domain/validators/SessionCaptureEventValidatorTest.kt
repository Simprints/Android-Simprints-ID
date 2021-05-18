package com.simprints.eventsystem.event.domain.validators

import com.simprints.id.sampledata.SampleDefaults.GUID1
import com.simprints.id.sampledata.SampleDefaults.GUID2
import com.simprints.id.commontesttools.events.createSessionCaptureEvent
import com.simprints.eventsystem.exceptions.validator.SessionEventCaptureAlreadyExists
import io.kotlintest.shouldThrow
import org.junit.Test

class SessionCaptureEventValidatorTest {

    @Test
    fun addSessionCaptureEvent_succeeds() {
        SessionCaptureEventValidator().validate(emptyList(), createSessionCaptureEvent())
    }

    @Test
    fun alreadySessionCaptureEventPresent_addNewSessionCaptureEvent_fails() {
        shouldThrow<SessionEventCaptureAlreadyExists> {
            SessionCaptureEventValidator().validate(
                listOf(createSessionCaptureEvent(GUID1)),
                createSessionCaptureEvent(GUID2)
            )
        }
    }

}
