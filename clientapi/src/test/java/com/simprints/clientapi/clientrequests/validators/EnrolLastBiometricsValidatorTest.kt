package com.simprints.clientapi.clientrequests.validators

import com.simprints.clientapi.clientrequests.extractors.EnrolLastBiometricsExtractor
import com.simprints.clientapi.exceptions.InvalidSessionIdException
import io.kotlintest.shouldThrow
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import java.util.*

class EnrolLastBiometricsValidatorTest {

    @MockK
    lateinit var enrolLastBiometricsExtractor: EnrolLastBiometricsExtractor
    private val sessionID = UUID.randomUUID().toString()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { enrolLastBiometricsExtractor.getProjectId() } returns "project_id"
        every { enrolLastBiometricsExtractor.getUserId() } returns "user_id"
    }

    @Test
    fun givenNotIdentificationAsLastFlow_enrolLastBiometricsReceived_shouldThrowAnException() {
        val enrolLastBiometricsValidator = EnrolLastBiometricsValidator(enrolLastBiometricsExtractor, sessionID, false)
        shouldThrow<InvalidSessionIdException> {
            enrolLastBiometricsValidator.validateClientRequest()
        }
    }

    @Test
    fun aRequestWithoutSessionIdReceived_shouldThrowAnException() {
        val enrolLastBiometricsValidator = EnrolLastBiometricsValidator(enrolLastBiometricsExtractor, sessionID, true)
        shouldThrow<InvalidSessionIdException> {
            enrolLastBiometricsValidator.validateClientRequest()
        }
    }
}
