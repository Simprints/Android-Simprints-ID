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
        val currentEvents = listOf(createSessionCaptureEvent(), createIdentificationCallbackEvent())
        validator.validate(currentEvents, createGuidSelectionEvent())
    }

    @Test
    fun validate_shouldThrowIfGuidEventIsAlreadyPresent() {
        shouldThrow<GuidSelectEventValidatorException> {
            val currentEvents = listOf(createSessionCaptureEvent(), createGuidSelectionEvent())
            validator.validate(currentEvents, createGuidSelectionEvent())
        }
    }

    @Test
    fun validate_shouldThrowIfIdentificationCallbackIsNotPresent() {
        shouldThrow<GuidSelectEventValidatorException> {
            val currentEvents = listOf(createSessionCaptureEvent())
            validator.validate(currentEvents, createGuidSelectionEvent())
        }
    }
}
