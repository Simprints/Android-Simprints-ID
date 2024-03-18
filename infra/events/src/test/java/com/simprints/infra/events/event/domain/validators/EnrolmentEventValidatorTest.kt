package com.simprints.infra.events.event.domain.validators

import com.simprints.infra.events.exceptions.validator.EnrolmentEventValidatorException
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.createEnrolmentEventV2
import com.simprints.infra.events.sampledata.createEventWithSessionId
import com.simprints.infra.events.sampledata.createFaceCaptureEvent
import com.simprints.infra.events.sampledata.createFingerprintCaptureEvent
import com.simprints.infra.events.sampledata.createPersonCreationEvent
import com.simprints.testtools.common.syntax.assertThrows
import org.junit.Before
import org.junit.Test

internal class EnrolmentEventValidatorTest {

    lateinit var validator: EnrolmentEventValidator

    @Before
    fun setUp() {
        validator = EnrolmentEventValidator()
    }

    @Test
    fun validate_shouldValidateIfBiometricCaptureAndPersonCreationIsPresent() {
        val currentEvents = listOf(createFaceCaptureEvent(), createPersonCreationEvent())
        validator.run { validate(currentEvents, createEnrolmentEventV2()) }
    }

    @Test
    fun validate_shouldThrowIfBiometricCaptureIsNotPresent() {
        assertThrows<EnrolmentEventValidatorException> {
            val currentEvents = listOf(createEventWithSessionId(GUID1, GUID1), createPersonCreationEvent())
            validator.validate(currentEvents, createEnrolmentEventV2())
        }
    }

    @Test
    fun validate_shouldThrowIfPersonCreationIsNotPresent() {
        assertThrows<EnrolmentEventValidatorException> {
            val currentEvents = listOf(createFingerprintCaptureEvent())
            validator.validate(currentEvents, createEnrolmentEventV2())
        }
    }

}
