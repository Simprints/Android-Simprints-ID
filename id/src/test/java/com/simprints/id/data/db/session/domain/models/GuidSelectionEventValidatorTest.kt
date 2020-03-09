package com.simprints.id.data.db.session.domain.models

import com.simprints.id.commontesttools.sessionEvents.createFakeOpenSession
import com.simprints.id.data.db.session.domain.models.events.GuidSelectionEvent
import com.simprints.id.data.db.session.domain.models.events.callback.IdentificationCallbackEvent
import com.simprints.id.exceptions.safe.session.validator.GuidSelectEventValidatorException
import com.simprints.id.tools.TimeHelperImpl
import io.kotlintest.shouldThrow
import org.junit.Test
import java.util.*

class GuidSelectionEventValidatorTest {

    val timeHelper = TimeHelperImpl()

    @Test
    fun validate_shouldRejectASessionWithMultipleGuidSelectionEvents() {
        val session = createFakeOpenSession(timeHelper)
        session.events.add(GuidSelectionEvent(0, UUID.randomUUID().toString()))
        session.events.add(GuidSelectionEvent(0, UUID.randomUUID().toString()))

        shouldThrow<GuidSelectEventValidatorException> {
            GuidSelectionEventValidator().validate(session)
        }
    }

    @Test
    fun validate_shouldRejectASessionWithNoIdentificationCallback() {
        val session = createFakeOpenSession(timeHelper)
        session.events.add(GuidSelectionEvent(0, UUID.randomUUID().toString()))

        shouldThrow<GuidSelectEventValidatorException> {
            GuidSelectionEventValidator().validate(session)
        }
    }

    @Test
    fun validate_shouldAcceptAValidSession() {
        val session = createFakeOpenSession(timeHelper)
        session.events.add(IdentificationCallbackEvent(0, UUID.randomUUID().toString(), emptyList()))
        session.events.add(GuidSelectionEvent(0, UUID.randomUUID().toString()))

        GuidSelectionEventValidator().validate(session)
    }
}
