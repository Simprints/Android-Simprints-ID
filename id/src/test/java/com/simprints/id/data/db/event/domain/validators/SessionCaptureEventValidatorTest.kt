package com.simprints.id.data.db.event.domain.validators

import com.simprints.id.commontesttools.DefaultTestConstants.GUID1
import com.simprints.id.commontesttools.DefaultTestConstants.GUID2
import com.simprints.id.commontesttools.events.createSessionCaptureEvent
import com.simprints.id.exceptions.safe.session.validator.SessionEventCaptureAlreadyExists
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
                SessionCaptureEventValidator().validate(listOf(createSessionCaptureEvent(GUID1)), createSessionCaptureEvent(GUID2))
        }
    }
}
