package com.simprints.id.data.db.event.domain.validators

import com.simprints.id.commontesttools.events.createGuidSelectionEvent
import com.simprints.id.commontesttools.events.createIdentificationCallbackEvent
import com.simprints.id.commontesttools.events.createSessionCaptureEvent
import com.simprints.id.exceptions.safe.session.validator.GuidSelectEventValidatorException
import io.kotlintest.shouldThrow
import org.junit.Before
import org.junit.Test

class GuidSelectionEventValidatorTest {

    lateinit var validator: GuidSelectionEventValidator

    @Before
    fun setUp() {
        validator = GuidSelectionEventValidator()
    }

    @Test
    fun validate_shouldValidateIfIdentificationEventIsPresent() {
        val sessionEvent = createSessionCaptureEvent()
        val currentEvents = createIdentificationCallbackEvent()
        validator.validate(sessionEvent, currentEvents)
    }

    @Test
    fun validate_shouldThrowIfGuidEventIsAlreadyPresent() {
        shouldThrow<GuidSelectEventValidatorException> {
            val sessionEvent = createSessionCaptureEvent()
            validator.validate(sessionEvent, createGuidSelectionEvent())
        }
    }

    @Test
    fun validate_shouldThrowIfIdentificationCallbackIsNotPresent() {
        shouldThrow<GuidSelectEventValidatorException> {
            val sessionEvent = createSessionCaptureEvent()
            validator.validate(sessionEvent, createGuidSelectionEvent())
        }
    }
}
