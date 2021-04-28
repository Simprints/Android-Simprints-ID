package com.simprints.id.data.db.event.domain.validators

import com.simprints.id.commontesttools.events.*
import com.simprints.id.exceptions.unexpected.session.validator.EnrolmentEventValidatorException
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
