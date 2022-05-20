package com.simprints.eventsystem.event.domain.validators

import com.simprints.eventsystem.exceptions.validator.EnrolmentEventValidatorException
import com.simprints.eventsystem.sampledata.createEnrolmentEventV2
import com.simprints.eventsystem.sampledata.createFaceCaptureEvent
import com.simprints.eventsystem.sampledata.createFaceCaptureEventV3
import com.simprints.eventsystem.sampledata.createFingerprintCaptureEvent
import com.simprints.eventsystem.sampledata.createPersonCreationEvent
import com.simprints.eventsystem.sampledata.createSessionCaptureEvent
import io.kotest.assertions.throwables.shouldThrow
import org.junit.Before
import org.junit.Test

class EnrolmentEventValidatorTest {

    lateinit var validator: EnrolmentEventValidator

    @Before
    fun setUp() {
        validator = EnrolmentEventValidator()
    }

    @Test
    fun validate_shouldValidateIfBiometricCaptureAndPersonCreationIsPresent() {
        val currentEvents = listOf(createFaceCaptureEventV3(), createPersonCreationEvent())
        validator.validate(currentEvents, createEnrolmentEventV2())
    }

    @Test
    fun validate_shouldThrowIfBiometricCaptureIsNotPresent() {
        shouldThrow<EnrolmentEventValidatorException> {
            val currentEvents = listOf(createSessionCaptureEvent(), createPersonCreationEvent())
            validator.validate(currentEvents, createEnrolmentEventV2())
        }
    }

    @Test
    fun validate_shouldThrowIfPersonCreationIsNotPresent() {
        shouldThrow<EnrolmentEventValidatorException> {
            val currentEvents = listOf(createFingerprintCaptureEvent())
            validator.validate(currentEvents, createEnrolmentEventV2())
        }
    }

}
