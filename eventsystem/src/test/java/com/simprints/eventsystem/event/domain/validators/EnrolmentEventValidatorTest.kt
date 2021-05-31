package com.simprints.eventsystem.event.domain.validators

import com.simprints.eventsystem.*
import com.simprints.eventsystem.exceptions.validator.EnrolmentEventValidatorException
import io.kotlintest.shouldThrow
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
        val currentEvents = listOf(createFaceCaptureEvent(), createPersonCreationEvent())
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
